package com.beducation.controller;

import com.beducation.model.*;
import com.beducation.model.Application.ApplicationStatus;
import com.beducation.service.AdminService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

/**
 * ============================================================
 * Controlador: AdminController
 * ============================================================
 * Panel de operaciones para un usuario ADMIN.
 * ============================================================
 */
@RestController
@RequestMapping("/admin")
@PreAuthorize("hasAuthority('SCOPE_ADMIN')") // o hasRole('ADMIN')
@RequiredArgsConstructor
@Tag(name = "Admin")
@SecurityRequirement(name = "bearer-key")
public class AdminController {

    private final AdminService adminService;
    private final com.beducation.security.SecurityUtils securityUtils;

    @GetMapping("/me")
    @Operation(summary = "Obtener info del administrador logueado")
    public ResponseEntity<User> getMyProfile() {
        return ResponseEntity.ok(securityUtils.getCurrentUser());
    }

    // ── APROBACIONES DE COMPAÑÍAS Y ESCUELAS ── //

    @GetMapping("/pending-schools")
    @Operation(summary = "Listar Escuelas en estado PENDING.")
    public ResponseEntity<Page<School>> getPendingSchools(Pageable pageable) {
        return ResponseEntity.ok(adminService.getPendingSchools(pageable));
    }

    @PatchMapping("/schools/{schoolId}/approve|reject")
    @Operation(summary = "Admitir/Rechazar incorporación (Stage 1)")
    public ResponseEntity<School> processSchool(
        @PathVariable Long schoolId,
        @RequestParam boolean approve,
        @RequestParam(required = false) String reason) {
        return ResponseEntity.ok(adminService.processSchoolApproval(schoolId, approve, reason));
    }

    @GetMapping("/pending-companies")
    @Operation(summary = "Listar Empresas en estado PENDING.")
    public ResponseEntity<Page<Company>> getPendingCompanies(Pageable pageable) {
        return ResponseEntity.ok(adminService.getPendingCompanies(pageable));
    }

    @PatchMapping("/companies/{companyId}/approve|reject")
    @Operation(summary = "Admitir/Rechazar nueva empresa")
    public ResponseEntity<Company> processCompany(
        @PathVariable Long companyId,
        @RequestParam boolean approve,
        @RequestParam(required = false) String reason) {
        return ResponseEntity.ok(adminService.processCompanyApproval(companyId, approve, reason));
    }

    // ── APROBACIONES DE OPORTUNIDADES Y FLUJO ── //

    @GetMapping("/pending-opportunities")
    @Operation(summary = "Listar Ofertas que requieren validez oficial")
    public ResponseEntity<Page<Opportunity>> getPendingOpportunities(Pageable pageable) {
        return ResponseEntity.ok(adminService.getPendingOpportunities(pageable));
    }

    @PatchMapping("/opportunities/{oppId}/approve|reject")
    @Operation(summary = "Garantizar calidad de oferta: aprueba y publica la Opportunity a la tabla publica")
    public ResponseEntity<Opportunity> processOpportunity(
        @PathVariable Long oppId,
        @RequestParam boolean approve,
        @RequestParam(required = false) String reason) {
        return ResponseEntity.ok(adminService.approveOpportunity(oppId, approve, reason));
    }

    @PatchMapping("/applications/{appId}/approve-stage4")
    @Operation(summary = "ADMIN APRUEBA EL PLACEMENT - Stage 4 PREVIA ESCUELA")
    public ResponseEntity<Application> preApprovePlacement(
        @PathVariable Long appId,
        @RequestParam boolean approve) {
        return ResponseEntity.ok(adminService.preApprovePlacement(appId, approve));
    }

    // ── DICCIONARIOS DE METADATA ── //

    @PostMapping("/keywords/category")
    @Operation(summary = "Alta de una nueva categoria organizativa (Informática, Sanidad...)")
    public ResponseEntity<KeywordCategory> addCategory(@RequestParam String name) {
        return ResponseEntity.ok(adminService.createKeywordCategory(name));
    }

    @PostMapping("/keywords/category/{categoryId}")
    @Operation(summary = "Alta de skill (Java, SAP, Costura...)")
    public ResponseEntity<Keyword> addKeyword(
        @PathVariable Long categoryId,
        @RequestParam String name) {
        return ResponseEntity.ok(adminService.createKeyword(name, categoryId));
    }

    @PostMapping("/education-types")
    @Operation(summary = "Dicionario homologado para tipos formativos: FP Básico, CFGS, Carrera Univ.")
    public ResponseEntity<EducationType> addEducationType(
        @RequestParam String name, @RequestParam String code, @RequestParam(required = false) String description) {
        return ResponseEntity.ok(adminService.createEducationType(name, code, description));
    }
}
