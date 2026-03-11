package com.beducation.service;

import com.beducation.dto.SchoolRegistrationDto;
import com.beducation.model.School;
import com.beducation.model.Student;
import com.beducation.model.User;
import com.beducation.repository.SchoolRepository;
import com.beducation.repository.StudentRepository;
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

    // ──────────────────────────────────────────────
    // GESTIÓN DE ESTUDIANTES POR LA ESCUELA
    // ──────────────────────────────────────────────

    /**
     * Invita a un único estudiante. Crea el registro Student y manda email.
     */
    public Student inviteStudent(Long schoolId, String firstName, String lastName, String email) {
        School school = getValidatedApprovedSchool(schoolId);

        if (studentRepository.existsByInvitationEmail(email) || userService.userRepository.existsByEmail(email)) {
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
     */
    public List<Student> importStudentsByCsv(Long schoolId, MultipartFile file) {
        School school = getValidatedApprovedSchool(schoolId);
        List<Student> importedStudents = new ArrayList<>();

        try (CSVReader reader = new CSVReader(new InputStreamReader(file.getInputStream()))) {
            List<String[]> rows = reader.readAll();
            
            // Omitir cabecera
            if (!rows.isEmpty()) rows.remove(0);

            if (rows.size() > 100) {
                throw new IllegalStateException("El archivo CSV contiene más de 100 registros. Límite excedido.");
            }

            for (String[] row : rows) {
                if(row.length >= 3) {
                    String firstName = row[0].trim();
                    String lastName = row[1].trim();
                    String email = row[2].trim();

                    if (!studentRepository.existsByInvitationEmail(email)) {
                        Student student = Student.builder()
                            .school(school)
                            .firstName(firstName)
                            .lastName(lastName)
                            .invitationEmail(email)
                            .invitedAt(LocalDateTime.now())
                            .build();
                        
                        studentRepository.save(student);
                        importedStudents.add(student);
                        // Encolar email async para no bloquear la petición masiva
                        emailService.sendStudentInvitationEmail(email, school);
                    }
                }
            }
            log.info("Importación CSV finalizada. {} estudiantes invitados a {}", importedStudents.size(), school.getName());
            return importedStudents;
            
        } catch (Exception e) {
            log.error("Fallo durante el procesado CSV", e);
            throw new RuntimeException("Error procesando el archivo CSV: " + e.getMessage());
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
