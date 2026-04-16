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
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

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
    private final UserService userService;
    private final EmailService emailService;
    private final StudentService studentService;

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
        
        // Delegar en StudentService para manejar la transacción independiente
        StudentService.InvitationResult result = studentService.inviteStudent(school, firstName, lastName, email, null);
        
        if (result != null) {
            emailService.sendStudentInvitationEmail(email, school);
            log.info("Estudiante invitado: {} por la escuela {}", email, school.getName());
            return result.student();
        }
        
        return null;
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

    /**
     * Obtiene estadísticas del funnel para los alumnos de la escuela.
     */
    @Transactional(readOnly = true)
    public java.util.Map<String, Object> getDashboardStats(Long schoolId) {
        School school = getValidatedApprovedSchool(schoolId);
        
        java.util.Map<String, Object> stats = new java.util.HashMap<>();
        
        // Cargar todos los estudiantes de la escuela
        List<Student> schoolStudents = studentRepository.findBySchoolId(school.getId());
        
        // Calcular Funnel de Aplicaciones Global de sus alumnos
        java.util.Map<String, Long> funnel = new java.util.HashMap<>();
        for (Application.ApplicationStatus status : Application.ApplicationStatus.values()) {
            funnel.put(status.name(), 0L);
        }

        long totalApps = 0;
        for (Student std : schoolStudents) {
            // we use applicationRepository to be more direct
            List<Application> apps = applicationRepository.findByStudentId(std.getId());
            for (Application app : apps) {
                String statusName = app.getStatus().name();
                funnel.put(statusName, funnel.get(statusName) + 1);
                totalApps++;
            }
        }

        stats.put("funnel", funnel);
        stats.put("totalApplications", totalApps);
        stats.put("totalStudents", (long) schoolStudents.size());
        stats.put("registeredStudents", schoolStudents.stream().filter(s -> s.getRegisteredAt() != null).count());
        
        return stats;
    }

    private School getValidatedApprovedSchool(Long schoolId) {
        School school = schoolRepository.findById(schoolId)
            .orElseThrow(() -> new RuntimeException("Escuela no encontrada: " + schoolId));
        if (school.getStatus() != School.ApprovalStatus.APPROVED) {
            throw new IllegalStateException("Tu escuela debe ser aprobada antes de realizar acciones.");
        }
        return school;
    }
}
