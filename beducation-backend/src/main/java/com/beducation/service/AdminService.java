package com.beducation.service;

import com.beducation.model.*;
import com.beducation.model.Application.ApplicationStatus;
import com.beducation.repository.*;
import com.opencsv.CSVReader;
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
 * Servicio: AdminService
 * ============================================================
 * Panel de control para administradores de la plataforma.
 * Responsabilidades:
 * - Aprobar/Rechazar Escuelas y Empresas (Stage 1 de registros).
 * - Aprobar/Rechazar Ofertas de prácticas (Publicaciones).
 * - Aprobar/Rechazar Placements en Etapa 4 del workflow.
 * - Gestionar diccionarios: Keywords y EducationTypes.
 * - Suspender usuarios (Escuelas, Estudiantes o Empresas).
 * ============================================================
 */
@Service
@RequiredArgsConstructor
@Slf4j
@Transactional
public class AdminService {

    private final SchoolRepository schoolRepository;
    private final CompanyRepository companyRepository;
    private final OpportunityRepository opportunityRepository;
    private final ApplicationRepository applicationRepository;
    private final KeywordCategoryRepository keywordCategoryRepository;
    private final KeywordRepository keywordRepository;
    private final EducationTypeRepository educationTypeRepository;
    private final StudentRepository studentRepository;
    private final GalleryAccessRequestRepository galleryAccessRequestRepository;
    private final UserService userService;
    private final EmailService emailService;
    private final SchoolService schoolService;
    private final StudentService studentService;

    // ──────────────────────────────────────────────
    // APROBACIONES DE ENTIDADES (Escuelas y Empresas)
    // ──────────────────────────────────────────────

    public School processSchoolApproval(Long schoolId, boolean approve, String reason) {
        School school = schoolRepository.findById(schoolId)
            .orElseThrow(() -> new RuntimeException("Escuela no encontrada"));

        if (approve) {
            school.setStatus(School.ApprovalStatus.APPROVED);
            school.setApprovedAt(LocalDateTime.now());
            // Activamos su usuario base
            userService.changeUserStatus(school.getUser().getId(), User.UserStatus.ACTIVE);
            log.info("Admin aprobó la escuela: {}", school.getName());
        } else {
            school.setStatus(School.ApprovalStatus.REJECTED);
            school.setRejectionReason(reason);
            // El usuario no podrá logearse con privilegios
            log.info("Admin rechazó la escuela: {}. Motivo: {}", school.getName(), reason);
        }
        return schoolRepository.save(school);
    }

    public Company processCompanyApproval(Long companyId, boolean approve, String reason) {
        Company company = companyRepository.findById(companyId)
            .orElseThrow(() -> new RuntimeException("Empresa no encontrada"));

        if (approve) {
            company.setStatus(School.ApprovalStatus.APPROVED);
            company.setApprovedAt(LocalDateTime.now());
            userService.changeUserStatus(company.getUser().getId(), User.UserStatus.ACTIVE);
            log.info("Admin aprobó la empresa: {}", company.getName());
        } else {
            company.setStatus(School.ApprovalStatus.REJECTED);
            company.setRejectionReason(reason);
            log.info("Admin rechazó la empresa: {}. Motivo: {}", company.getName(), reason);
        }
        return companyRepository.save(company);
    }

    // ──────────────────────────────────────────────
    // APROBACIONES DEL WORKFLOW Y PUBLICACIONES
    // ──────────────────────────────────────────────

    /** Publicar/Aprobar una oferta de prácticas (Opportunity) de estado PENDING_REVIEW a APPROVED */
    public Opportunity approveOpportunity(Long oppId, boolean approve, String reason) {
        Opportunity opp = opportunityRepository.findById(oppId)
            .orElseThrow(() -> new RuntimeException("Oferta no encontrada"));

        if (approve) {
            opp.setStatus(Opportunity.OpportunityStatus.APPROVED);
            opp.setApprovedAt(LocalDateTime.now());
            log.info("Admin aprobó la oferta ID: {}", oppId);
        } else {
            opp.setStatus(Opportunity.OpportunityStatus.DRAFT);
            opp.setRejectionReason(reason);
            log.info("Admin rechazó publicar la oferta ID: {}", oppId);
        }
        return opportunityRepository.save(opp);
    }

    /** Pre-aprobación del Admin en la Etapa 4 del placement para notificar a la Escuela (Etapa 5) */
    public Application preApprovePlacement(Long applicationId, boolean approve) {
        Application app = applicationRepository.findById(applicationId)
            .orElseThrow(() -> new RuntimeException("Solicitud no existente"));

        if (app.getStatus() != ApplicationStatus.ADMIN_APPROVED) {
            throw new IllegalStateException("La solicitud no procede en Stage 4.");
        }

        if (approve) {
            log.info("Admin Aprobó Stage 4 de Solicitud {}. Enviando a Escuela.", applicationId);
            emailService.sendFinalApprovalRequestEmail(app.getStudent().getSchool(), app);
        } else {
            app.setStatus(ApplicationStatus.REJECTED);
            app.setRejectionReason("Rechazado en etapa de validación Administrativa (Admin).");
            app.setStatusUpdatedAt(LocalDateTime.now());
        }
        
        return applicationRepository.save(app);
    }

    // ──────────────────────────────────────────────
    // MANTENIMIENTO DEL DICCIONARIO: KEYWORDS Y CATEGORÍAS
    // ──────────────────────────────────────────────

    public KeywordCategory createKeywordCategory(String name) {
        if (keywordCategoryRepository.existsByName(name)) {
            throw new IllegalStateException("La categoría analítica/keyword ya existe.");
        }
        KeywordCategory cat = KeywordCategory.builder().name(name).build();
        return keywordCategoryRepository.save(cat);
    }

    public Keyword createKeyword(String name, Long categoryId) {
        KeywordCategory category = keywordCategoryRepository.findById(categoryId)
            .orElseThrow(() -> new RuntimeException("Categoría no encontrada."));

        Keyword keyword = Keyword.builder()
            .name(name)
            .category(category)
            .build();
        return keywordRepository.save(keyword);
    }

    // ──────────────────────────────────────────────
    // MANTENIMIENTO DEL DICCIONARIO: TIPOS DE FORMACIÓN
    // ──────────────────────────────────────────────

    public EducationType createEducationType(String name, String code, String desc) {
        if (educationTypeRepository.existsByCode(code)) {
            throw new IllegalStateException("Ese código de Formación base ya existe en la DB.");
        }
        EducationType type = EducationType.builder()
            .name(name)
            .code(code)
            .description(desc)
            .build();
        return educationTypeRepository.save(type);
    }

    // ──────────────────────────────────────────────
    // LISTADOS PAGINADOS PARA EL DASHBOARD DE ADMIN
    // ──────────────────────────────────────────────

    @Transactional(readOnly = true)
    public Page<School> getPendingSchools(Pageable pageable) {
        return schoolRepository.findByStatus(School.ApprovalStatus.PENDING, pageable);
    }

    @Transactional(readOnly = true)
    public Page<Company> getPendingCompanies(Pageable pageable) {
        return companyRepository.findByStatus(School.ApprovalStatus.PENDING, pageable);
    }

    @Transactional(readOnly = true)
    public Page<Opportunity> getPendingOpportunities(Pageable pageable) {
        return opportunityRepository.findByStatus(Opportunity.OpportunityStatus.PENDING_REVIEW, pageable);
    }

    /** Obtener aplicaciones que requieren validación de Admin (Stage 4) */
    @Transactional(readOnly = true)
    public Page<Application> getPendingPlacements(Pageable pageable) {
        return applicationRepository.findByStatus(ApplicationStatus.STUDENT_ACCEPTED, pageable);
    }

    /**
     * Resumen estadístico del sistema para el Dashboard de Admin.
     */
    @Transactional(readOnly = true)
    public java.util.Map<String, Object> getDashboardStats() {
        java.util.Map<String, Object> stats = new java.util.HashMap<>();
        
        // Contadores Globales de Activos
        stats.put("totalSchools", schoolRepository.countByStatus(School.ApprovalStatus.APPROVED));
        stats.put("totalCompanies", companyRepository.countByStatus(School.ApprovalStatus.APPROVED));
        stats.put("totalOpportunities", opportunityRepository.countByStatus(Opportunity.OpportunityStatus.APPROVED));
        stats.put("totalApplications", applicationRepository.count());
        stats.put("totalStudents", studentRepository.count());
        
        // Funnel de Aplicaciones
        java.util.Map<String, Long> funnel = new java.util.HashMap<>();
        for (Application.ApplicationStatus status : Application.ApplicationStatus.values()) {
            funnel.put(status.name(), applicationRepository.countByStatus(status));
        }
        stats.put("funnel", funnel);
        
        // Badges (Notificaciones Pendientes de Acción por el Admin)
        stats.put("pendingSchoolsCount", schoolRepository.countByStatus(School.ApprovalStatus.PENDING));
        stats.put("pendingCompaniesCount", companyRepository.countByStatus(School.ApprovalStatus.PENDING));
        stats.put("pendingOpportunitiesCount", opportunityRepository.countByStatus(Opportunity.OpportunityStatus.PENDING_REVIEW));
        
        // Etapa 4: El Administrador debe validar el acuerdo antes de mandarlo a la Escuela
        stats.put("pendingStage4Count", applicationRepository.countByStatus(ApplicationStatus.STUDENT_ACCEPTED));

        // Solicitudes de Galería
        stats.put("pendingGalleryRequestsCount", galleryAccessRequestRepository.countByStatus(GalleryAccessRequest.AccessStatus.PENDING));

        return stats;
    }

    // ──────────────────────────────────────────────
    // LISTADOS TOTALES PARA EL ADMIN
    // ──────────────────────────────────────────────

    @Transactional(readOnly = true)
    public Page<School> getAllSchools(Pageable pageable) {
        return schoolRepository.findAll(pageable);
    }

    @Transactional(readOnly = true)
    public Page<Company> getAllCompanies(Pageable pageable) {
        return companyRepository.findAll(pageable);
    }

    @Transactional(readOnly = true)
    public Page<Student> getAllStudents(Pageable pageable) {
        return studentRepository.findAll(pageable);
    }

    /**
     * Cambiar estado de activación de cualquier usuario.
     */
    public User changeUserStatus(Long userId, User.UserStatus status) {
        return userService.changeUserStatus(userId, status);
    }

    /**
     * Eliminar un usuario y sus registros asociados.
     */
    public void deleteUser(Long userId) {
        schoolRepository.findByUserId(userId).ifPresent(schoolRepository::delete);
        companyRepository.findByUserId(userId).ifPresent(companyRepository::delete);
        studentRepository.findByUserId(userId).ifPresent(studentRepository::delete);
        
        userService.deleteUser(userId);
        log.info("Usuario con ID {} y sus datos asociados eliminados por el Administrador.", userId);
    }

    /**
     * El Admin invita directamente a un alumno, asociándolo a una escuela.
     */
    public Student inviteStudent(Long schoolId, String first, String last, String email) {
        return schoolService.inviteStudent(schoolId, first, last, email);
    }

    /**
     * Importación masiva por CSV (hasta 100 estudiantes) realizada por el Admin.
     * El Admin debe especificar el ID de la escuela.
     */
    public List<Student> importStudentsByCsv(Long schoolId, MultipartFile file) {
        School school = schoolRepository.findById(schoolId)
            .orElseThrow(() -> new RuntimeException("Escuela no encontrada: " + schoolId));
            
        List<Student> importedStudents = new ArrayList<>();

        try {
            byte[] bytes = file.getBytes();
            String content = new String(bytes, java.nio.charset.StandardCharsets.UTF_8);
            if (content.startsWith("\uFEFF")) {
                content = content.substring(1);
            }

            String[] lines = content.split("\\r?\\n");
            if (lines.length == 0) return importedStudents;

            String firstLine = lines[0];
            char separator = ',';
            long semiColons = firstLine.chars().filter(ch -> ch == ';').count();
            long commas = firstLine.chars().filter(ch -> ch == ',').count();
            if (semiColons > commas) {
                separator = ';';
            }

            com.opencsv.CSVParser parser = new com.opencsv.CSVParserBuilder()
                .withSeparator(separator)
                .build();

            try (CSVReader reader = new com.opencsv.CSVReaderBuilder(new java.io.StringReader(content))
                    .withCSVParser(parser)
                    .build()) {
                
                List<String[]> rows = reader.readAll();
                if (!rows.isEmpty()) rows.remove(0); // Quitar cabecera

                if (rows.size() > 100) {
                    throw new IllegalStateException("El archivo CSV contiene más de 100 registros. Límite: 100.");
                }

                int rowIdx = 1;
                for (String[] row : rows) {
                    rowIdx++;
                    if (row.length < 3) continue;

                    String firstName = row[0].trim();
                    String lastName = row[1].trim();
                    String email = row[2].trim().toLowerCase();
                    String eduCode = row.length >= 4 ? row[3].trim() : null;

                    if (email.isEmpty()) continue;

                    try {
                        StudentService.InvitationResult result = studentService.inviteStudent(school, firstName, lastName, email, eduCode);
                        if (result != null) {
                            importedStudents.add(result.student());
                            emailService.sendStudentInvitationEmail(email, school);
                        }
                    } catch (Exception e) {
                        log.error("Fallo al procesar estudiante en fila {}: {}", rowIdx, e.getMessage());
                    }
                }
            }
            return importedStudents;
        } catch (Exception e) {
            log.error("Error crítico procesando CSV desde Admin", e);
            throw new RuntimeException("Error procesando el archivo CSV: " + e.getMessage());
        }
    }
}
