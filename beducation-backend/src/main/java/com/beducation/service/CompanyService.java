package com.beducation.service;

import com.beducation.dto.CompanyRegistrationDto;
import com.beducation.dto.OpportunityDto;
import com.beducation.model.*;
import com.beducation.repository.*;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.time.LocalDateTime;
import java.util.List;

/**
 * ============================================================
 * Servicio: CompanyService
 * ============================================================
 * Maneja el registro de empresas candidatas en la plataforma.
 * Administra el CRUD de ofertas (Opportunities) y su ciclo de vida
 * (DRAFT, PENDING_REVIEW, APPROVED) por parte del control de calidad.
 * ============================================================
 */
@Service
@RequiredArgsConstructor
@Slf4j
@Transactional
public class CompanyService {

    private final CompanyRepository companyRepository;
    private final OpportunityRepository opportunityRepository;
    private final KeywordRepository keywordRepository;
    private final EducationTypeRepository educationTypeRepository;
    private final UserService userService;
    private final S3StorageService s3StorageService;

    // ──────────────────────────────────────────────
    // REGISTRO DE EMPRESAS Y PERFIL
    // ──────────────────────────────────────────────

    /**
     * PASO 1 (Mismo flujo que School): Formulario público de registro
     * Crea entidad en estado PENDING, ligada al User base.
     */
    public Company registerCompany(CompanyRegistrationDto dto) {
        // En primer lugar, crea el usuario base para las credenciales de entrada.
        User user = userService.createUser(dto.getAuth0Id(), dto.getEmail(), User.Role.COMPANY);

        Company company = Company.builder()
            .user(user)
            .name(dto.getName())
            .country(dto.getCountry())
            .city(dto.getCity())
            .website(dto.getWebsite())
            .description(dto.getDescription())
            .companySize(dto.getCompanySize())
            .sector(dto.getSector())
            .status(School.ApprovalStatus.PENDING)
            .build();

        log.info("Empresa {} registrada y pendiente de aprobación del administrador.", company.getName());
        return companyRepository.save(company);
    }

    /**
     * PASO 2: Aprobada/Rechazada por Admin
     */
    public Company approveOrRejectCompany(Long companyId, School.ApprovalStatus newStatus, String reason) {
        Company company = companyRepository.findById(companyId)
            .orElseThrow(() -> new RuntimeException("Empresa no existente."));
        
        company.setStatus(newStatus);
        if (newStatus == School.ApprovalStatus.REJECTED) company.setRejectionReason(reason);
        if (newStatus == School.ApprovalStatus.APPROVED) company.setApprovedAt(LocalDateTime.now());
        
        return companyRepository.save(company);
    }

    /** Mantenimiento de información de empresa tras aprobación */
    public Company updateProfile(Long companyId, CompanyRegistrationDto changes) {
        Company company = checkCompanyActiveStatus(companyId);
        
        if (changes.getDescription() != null) company.setDescription(changes.getDescription());
        if (changes.getWebsite() != null) company.setWebsite(changes.getWebsite());
        if (changes.getCompanySize() != null) company.setCompanySize(changes.getCompanySize());
        company.setUpdatedAt(LocalDateTime.now());

        return companyRepository.save(company);
    }
    
    /** Sube a AWS S3 la metadata local que devuelve la clave de visualización **/
    public Company uploadCompanyLogo(Long companyId, MultipartFile file) {
    	Company company = checkCompanyActiveStatus(companyId);
    	String key = s3StorageService.uploadFile(file, "companies/" + company.getUser().getAuth0Id() + "/logos");
    	company.setLogoS3Key(key);
    	return companyRepository.save(company);
    }

    /** Obtiene perfil de empresa por ID de usuario base **/
    @Transactional(readOnly = true)
    public Company getCompanyByUserId(Long userId) {
        return companyRepository.findByUserId(userId)
            .orElseThrow(() -> new RuntimeException("No se encontró perfil de empresa para el usuario ID: " + userId));
    }

    // ──────────────────────────────────────────────
    // CICLO DE VIDA CRUD DE OPORTUNIDADES (Opportunities)
    // ──────────────────────────────────────────────

    /**
     * Crear u ofrecer una oportunidad de prácticas "en borrador/DRAFT".
     * O enviarla directamente al estado PENDING_REVIEW 
     * al publicar indicando el state apropiado en el DTO.
     */
    public Opportunity submitOpportunity(Long companyId, OpportunityDto dto) {
        Company company = checkCompanyActiveStatus(companyId);

        Opportunity opportunity = Opportunity.builder()
            .company(company)
            .title(dto.getTitle())
            .description(dto.getDescription())
            .country(dto.getCountry())
            .city(dto.getCity())
            .startDate(dto.getStartDate())
            .endDate(dto.getEndDate())
            .spots(dto.getSpots() != null ? dto.getSpots() : 1)
            .spotsAvailable(dto.getSpots() != null ? dto.getSpots() : 1)
            .status(dto.getStatus() != null ? dto.getStatus() : Opportunity.OpportunityStatus.DRAFT)
            .build();

        // Enlazando el tipo de formación y keywords extraídos de DTO
        if (dto.getEducationTypeId() != null) {
            EducationType et = educationTypeRepository.findById(dto.getEducationTypeId()).orElse(null);
            opportunity.setEducationType(et);
        }
        if (dto.getKeywordIds() != null && !dto.getKeywordIds().isEmpty()) {
            List<Keyword> kws = keywordRepository.findAllById(dto.getKeywordIds());
            opportunity.setKeywords(kws);
        }

        log.info("La Empresa {} creó una oportunidad de título: {} (Estado: {})", 
                 company.getName(), opportunity.getTitle(), opportunity.getStatus());
                 
        return opportunityRepository.save(opportunity);
    }

    /**
     * Empresa edita la información de su oferta. Puede requerir
     * volver al estado de Review, el controlador lo evalúa.
     */
    public Opportunity updateOpportunity(Long oppId, Long companyId, OpportunityDto dto) {
        Opportunity opp = opportunityRepository.findById(oppId)
            .orElseThrow(() -> new RuntimeException("Oferta no encontrada con ID: " + oppId));
        
        if (!opp.getCompany().getId().equals(companyId)) {
            throw new IllegalStateException("No tienes permiso de autoría sobre esta Oferta.");
        }

        if (dto.getTitle() != null) opp.setTitle(dto.getTitle());
        if (dto.getDescription() != null) opp.setDescription(dto.getDescription());
        if (dto.getStartDate() != null) opp.setStartDate(dto.getStartDate());
        if (dto.getEndDate() != null) opp.setEndDate(dto.getEndDate());
        if (dto.getStatus() != null) opp.setStatus(dto.getStatus());
        
        return opportunityRepository.save(opp);
    }

    /** Lista paginada para que la empresa visualice su Dashboard (Mis Ofertas) */
    @Transactional(readOnly = true)
    public Page<Opportunity> getCompanyOpportunities(Long companyId, Pageable pageable) {
        return opportunityRepository.findByCompanyId(companyId, pageable);
    }

    // ──────────────────────────────────────────────
    // Funciones Auxiliares
    // ──────────────────────────────────────────────

    private Company checkCompanyActiveStatus(Long companyId) {
        Company company = companyRepository.findById(companyId)
            .orElseThrow(() -> new RuntimeException("Empresa solicitada no existente."));
            
        if (company.getStatus() != School.ApprovalStatus.APPROVED) {
            throw new IllegalStateException("La empresa está bloqueada en un estado no Aprobado.");
        }
        return company;
    }
}
