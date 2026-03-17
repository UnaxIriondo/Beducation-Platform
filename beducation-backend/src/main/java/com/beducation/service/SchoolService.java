package com.beducation.service;

import com.beducation.dto.SchoolRegistrationDto;
import com.beducation.dto.SchoolProfileDto;
import com.beducation.model.Application;
import com.beducation.model.School;
import com.beducation.model.Student;
import com.beducation.model.User;
import com.beducation.repository.ApplicationRepository;
import com.beducation.repository.SchoolRepository;
import com.beducation.repository.StudentRepository;
import com.beducation.repository.EducationTypeRepository;
import com.opencsv.CSVReader;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.io.InputStreamReader;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

/**
 * ============================================================
 * Servicio: SchoolService
 * ============================================================
 * Maneja el registro de escuelas, el panel de gestión, y la 
 * invitación de estudiantes manual y masiva (CSV).
 * ============================================================
 */
@Service
@RequiredArgsConstructor
@Slf4j
@Transactional
public class SchoolService {

    private final SchoolRepository schoolRepository;
    private final StudentRepository studentRepository;
    private final ApplicationRepository applicationRepository;
    private final EducationTypeRepository educationTypeRepository;
    private final UserService userService;
    private final EmailService emailService;

    // ──────────────────────────────────────────────
    // REGISTRO Y APROBACIÓN DE ESCUELAS
    // ──────────────────────────────────────────────

    /**
     * PASO 1: Cuando la escuela rellena el formulario público de registro.
     * Genera un User y la entidad School en estado PENDING.
     */
    public School registerSchool(SchoolRegistrationDto dto) {
        // En primer lugar, crea el usuario base (se espera que en el controlador 
        // reciba auth0Id si es post-login o se asigne post-registro en un webhook)
        User user = userService.createUser(dto.getAuth0Id(), dto.getEmail(), User.Role.SCHOOL);

        School school = School.builder()
            .user(user)
            .name(dto.getName())
            .country(dto.getCountry())
            .city(dto.getCity())
            .address(dto.getAddress())
            .website(dto.getWebsite())
            .contactPerson(dto.getContactPerson())
            .phone(dto.getPhone())
            .status(School.ApprovalStatus.PENDING)
            .build();

        log.info("Escuela {} registrada y pendiente de aprobación.", school.getName());
        return schoolRepository.save(school);
    }

    /**
     * PASO 2: (Admin) Aprueba una escuela PENDING.
     */
    public School approveSchool(Long schoolId) {
        School school = schoolRepository.findById(schoolId)
            .orElseThrow(() -> new RuntimeException("Escuela no encontrada"));
            
        school.setStatus(School.ApprovalStatus.APPROVED);
        school.setApprovedAt(LocalDateTime.now());
        
        log.info("Escuela {} APROBADA por el administrador.", school.getName());
        return schoolRepository.save(school);
    }

    /**
     * PASO 2: (Admin) Rechaza una escuela.
     */
    public School rejectSchool(Long schoolId, String reason) {
        School school = schoolRepository.findById(schoolId)
            .orElseThrow(() -> new RuntimeException("Escuela no encontrada"));

        school.setStatus(School.ApprovalStatus.REJECTED);
        school.setRejectionReason(reason);
        return schoolRepository.save(school);
    }

    /**
     * Obtiene la escuela vinculada a un usuario.
     */
    @Transactional(readOnly = true)
    public School getSchoolByUserId(Long userId) {
        return schoolRepository.findByUserId(userId)
            .orElseThrow(() -> new RuntimeException("No se encontró perfil de escuela para este usuario."));
    }

    /**
     * Actualiza el perfil de la escuela.
     */
    public School updateSchoolProfile(Long schoolId, SchoolProfileDto dto) {
        School school = schoolRepository.findById(schoolId)
            .orElseThrow(() -> new RuntimeException("Escuela no encontrada: " + schoolId));

        school.setName(dto.getName());
        school.setInstitutionType(dto.getInstitutionType());
        school.setDescription(dto.getDescription());
        school.setPhone(dto.getPhone());
        school.setAddress(dto.getAddress());
        school.setWebsite(dto.getWebsite());
        school.setContactPerson(dto.getContactPerson());

        // También actualizamos el email del usuario base si ha cambiado
        User user = school.getUser();
        if (dto.getContactEmail() != null && !dto.getContactEmail().equals(user.getEmail())) {
            // Verificar si el nuevo email ya está en uso
            if (userService.existsByEmail(dto.getContactEmail())) {
                throw new IllegalStateException("El email " + dto.getContactEmail() + " ya está en uso por otro usuario.");
            }
            user.setEmail(dto.getContactEmail());
        }

        log.info("Perfil de escuela {} actualizado.", school.getName());
        return schoolRepository.save(school);
    }

    // ──────────────────────────────────────────────
    // GESTIÓN DE ESTUDIANTES POR LA ESCUELA
    // ──────────────────────────────────────────────

    /**
     * Invita a un único estudiante. Crea el registro Student y manda email.
     */
    public Student inviteStudent(Long schoolId, String firstName, String lastName, String email) {
        School school = getValidatedApprovedSchool(schoolId);

        if (studentRepository.existsByInvitationEmail(email) || userService.existsByEmail(email)) {
            throw new IllegalStateException("El email " + email + " ya está invitado o registrado en la plataforma.");
        }

        Student student = Student.builder()
            .school(school)
            .firstName(firstName)
            .lastName(lastName)
            .invitationEmail(email)
            .invitedAt(LocalDateTime.now())
            // Los estados de completitud arrancan en false (defecto)
            .build();

        Student saved = studentRepository.save(student);
        emailService.sendStudentInvitationEmail(email, school);

        log.info("Estudiante invitado: {} por la escuela {}", email, school.getName());
        return saved;
    }

    /**
     * Importación masiva por CSV (hasta 100 estudiantes).
     * El CSV debe tener cabeceras: firstName, lastName, email.
     * Soporta tanto comas (,) como punto y coma (;).
     */
    public List<Student> importStudentsByCsv(Long schoolId, MultipartFile file) {
        School school = getValidatedApprovedSchool(schoolId);
        List<Student> importedStudents = new ArrayList<>();

        // Detectar si el CSV usa coma o punto y coma leyendo la primera línea
        char separator = ',';
        try {
            byte[] bytes = file.getBytes();
            String content = new String(bytes);
            String firstLine = content.split("\n")[0];
            if (firstLine.contains(";") && !firstLine.contains(",")) {
                separator = ';';
            }
            log.info("Detectado separador de CSV: '{}'", separator);
        } catch (Exception e) {
            log.warn("No se pudo detectar el separador, usando coma por defecto");
        }

        com.opencsv.CSVParser parser = new com.opencsv.CSVParserBuilder()
            .withSeparator(separator)
            .build();

        try (CSVReader reader = new com.opencsv.CSVReaderBuilder(new InputStreamReader(file.getInputStream()))
                .withCSVParser(parser)
                .build()) {
            
            List<String[]> rows = reader.readAll();
            
            // Omitir cabecera
            if (!rows.isEmpty()) rows.remove(0);

            if (rows.size() > 100) {
                throw new IllegalStateException("El archivo CSV contiene más de 100 registros. Límite excedido.");
            }

            int rowIdx = 1;
            for (String[] row : rows) {
                rowIdx++;
                try {
                    if (row.length >= 3) {
                        String firstName = row[0].trim();
                        String lastName = row[1].trim();
                        String email = row[2].trim();

                        if (email.isEmpty()) continue;

                        if (!studentRepository.existsByInvitationEmail(email)) {
                            Student.StudentBuilder studentBuilder = Student.builder()
                                .school(school)
                                .firstName(firstName)
                                .lastName(lastName)
                                .invitationEmail(email)
                                .invitedAt(LocalDateTime.now());
                            
                            // Columna opcional de EducationType
                            if (row.length >= 4) {
                                String eduCode = row[3].trim();
                                if (!eduCode.isEmpty()) {
                                    educationTypeRepository.findByCode(eduCode)
                                        .ifPresent(studentBuilder::educationType);
                                }
                            }

                            Student student = studentBuilder.build();
                            studentRepository.save(student);
                            importedStudents.add(student);
                            emailService.sendStudentInvitationEmail(email, school);
                        } else {
                            log.info("Fila {}: El estudiante con email {} ya existe. Saltando.", rowIdx, email);
                        }
                    } else {
                        log.warn("Fila {}: Formato incorrecto. Se esperan al menos 3 columnas.", rowIdx);
                    }
                } catch (Exception e) {
                    log.error("Error procesando fila {}: {}", rowIdx, e.getMessage());
                }
            }
            log.info("Importación CSV finalizada. {} estudiantes invitados a {}", importedStudents.size(), school.getName());
            return importedStudents;
            
        } catch (Exception e) {
            log.error("Fallo crítico durante el procesado CSV", e);
            throw new RuntimeException("Error grave procesando el archivo CSV: " + e.getMessage());
        }
    }

    /**
     * Obtiene el listado de estudiantes de la escuela en el dashboard.
     */
    @Transactional(readOnly = true)
    public Page<Student> getSchoolStudents(Long schoolId, Pageable pageable) {
        School school = getValidatedApprovedSchool(schoolId);
        return studentRepository.findBySchoolId(school.getId(), pageable);
    }

    /**
     * Obtiene las aplicaciones pendientes de aprobación por la escuela.
     */
    @Transactional(readOnly = true)
    public Page<Application> getPendingApplications(Long schoolId, Pageable pageable) {
        School school = getValidatedApprovedSchool(schoolId);
        return applicationRepository.findPendingSchoolApprovalsBySchoolId(school.getId(), pageable);
    }

    /**
     * Elimina un estudiante de la escuela.
     */
    public void deleteStudent(Long schoolId, Long studentId) {
        School school = getValidatedApprovedSchool(schoolId);
        Student student = studentRepository.findById(studentId)
            .orElseThrow(() -> new RuntimeException("Estudiante no encontrado: " + studentId));

        if (!student.getSchool().getId().equals(school.getId())) {
            throw new RuntimeException("El estudiante no pertenece a esta escuela.");
        }

        studentRepository.delete(student);
        log.info("Estudiante {} eliminado por la escuela {}", studentId, school.getName());
    }

    // ──────────────────────────────────────────────
    // Helper Methods
    // ──────────────────────────────────────────────

    private School getValidatedApprovedSchool(Long schoolId) {
        School school = schoolRepository.findById(schoolId)
            .orElseThrow(() -> new RuntimeException("Escuela no encontrada: " + schoolId));
        if (school.getStatus() != School.ApprovalStatus.APPROVED) {
            throw new IllegalStateException("Tu escuela debe ser aprobada antes de realizar acciones.");
        }
        return school;
    }
}
