package com.beducation.controller;

import com.beducation.dto.CompanyRegistrationDto;
import com.beducation.dto.OpportunityDto;
import com.beducation.model.Company;
import com.beducation.model.Opportunity;
import com.beducation.service.CompanyService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

/**
 * ============================================================
 * Controlador: CompanyController
 * ============================================================
 * Endpoints REST del Portal de Empresa.
 * ============================================================
 */
@RestController
@RequestMapping("/companies")
@RequiredArgsConstructor
@Tag(name = "Company Portal", description = "Endpoints de empresa: perfil, logos (AWS), publicaciones/ofertas de prácticas.")
public class CompanyController {

    private final CompanyService companyService;
    private final com.beducation.security.SecurityUtils securityUtils;

    @GetMapping("/me")
    @PreAuthorize("hasAuthority('SCOPE_COMPANY')")
    @Operation(summary = "Obtener perfil de la empresa logueada", security = @SecurityRequirement(name = "bearerAuth"))
    public ResponseEntity<Company> getMyProfile() {
        System.out.println("DEBUG: CompanyController.getMyProfile() invocado");
        com.beducation.model.User user = securityUtils.getCurrentUser();
        return ResponseEntity.ok(companyService.getCompanyByUserId(user.getId()));
    }

    @PostMapping
    @Operation(summary = "Registro inicial público de una Empresa", description = "Registra una compañia que requiere aprobación administrativa.")
    public ResponseEntity<Company> registerCompany(@RequestBody @Valid CompanyRegistrationDto dto) {
        Company company = companyService.registerCompany(dto);
        return new ResponseEntity<>(company, HttpStatus.CREATED);
    }

    @PutMapping("/{companyId}")
    @PreAuthorize("hasAuthority('SCOPE_COMPANY')")
    @Operation(summary = "Editar perfil o description en Portal Empresa", security = @SecurityRequirement(name = "bearerAuth"))
    public ResponseEntity<Company> updateProfile(
        @PathVariable Long companyId,
        @RequestBody CompanyRegistrationDto dto) {
        return ResponseEntity.ok(companyService.updateProfile(companyId, dto));
    }

    @PostMapping("/{companyId}/logo")
    @PreAuthorize("hasAuthority('SCOPE_COMPANY')")
    @Operation(summary = "Subir Logo a S3 para visualización pública", security = @SecurityRequirement(name = "bearerAuth"))
    public ResponseEntity<Company> uploadLogo(
        @PathVariable Long companyId,
        @RequestPart("file") MultipartFile file) {
        return ResponseEntity.ok(companyService.uploadCompanyLogo(companyId, file));
    }

    // ──────────────────────────────────────────────
    // OPORTUNIDADES
    // ──────────────────────────────────────────────

    @PostMapping("/{companyId}/opportunities")
    @PreAuthorize("hasAuthority('SCOPE_COMPANY')")
    @Operation(summary = "Crear o enviar a Revisión (depende del OpportunityStatus del DTO) una Oferta.", security = @SecurityRequirement(name = "bearerAuth"))
    public ResponseEntity<Opportunity> createOpportunity(
        @PathVariable Long companyId,
        @RequestBody @Valid OpportunityDto dto) {
        return new ResponseEntity<>(companyService.submitOpportunity(companyId, dto), HttpStatus.CREATED);
    }

    @PutMapping("/{companyId}/opportunities/{oppId}")
    @PreAuthorize("hasAuthority('SCOPE_COMPANY')")
    @Operation(summary = "Editar Oferta existente", security = @SecurityRequirement(name = "bearerAuth"))
    public ResponseEntity<Opportunity> updateOpportunity(
        @PathVariable Long companyId,
        @PathVariable Long oppId,
        @RequestBody OpportunityDto dto) {
        return ResponseEntity.ok(companyService.updateOpportunity(oppId, companyId, dto));
    }

    @GetMapping("/{companyId}/opportunities")
    @PreAuthorize("hasAuthority('SCOPE_COMPANY')")
    @Operation(summary = "Obtener lista de oportunidades - Dashboard.", security = @SecurityRequirement(name = "bearerAuth"))
    public ResponseEntity<Page<Opportunity>> getCompanyOpportunities(
        @PathVariable Long companyId, Pageable pageable) {
        return ResponseEntity.ok(companyService.getCompanyOpportunities(companyId, pageable));
    }
}
