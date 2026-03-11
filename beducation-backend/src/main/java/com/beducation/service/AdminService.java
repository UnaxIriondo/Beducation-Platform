package com.beducation.service;

import com.beducation.model.*;
import com.beducation.model.Application.ApplicationStatus;
import com.beducation.repository.*;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;

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
    private final UserService userService;
    private final EmailService emailService;

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
}
