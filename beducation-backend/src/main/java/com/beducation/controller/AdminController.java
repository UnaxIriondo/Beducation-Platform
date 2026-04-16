package com.beducation.controller;

import com.beducation.model.*;
import com.beducation.service.AdminService;
import com.beducation.security.SecurityUtils;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;

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
    private final SecurityUtils securityUtils;

    @GetMapping("/me")
    @Operation(summary = "Obtener info del administrador logueado")
    public ResponseEntity<User> getMyProfile() {
        return ResponseEntity.ok(securityUtils.getCurrentUser());
    }

    @GetMapping("/stats")
    @Operation(summary = "Resumen estadístico del sistema para el Dashboard.")
    public ResponseEntity<java.util.Map<String, Object>> getStats() {
        return ResponseEntity.ok(adminService.getDashboardStats());
    }

    // ── APROBACIONES DE COMPAÑÍAS Y ESCUELAS ── //

    @GetMapping("/pending-schools")
    @Operation(summary = "Listar Escuelas en estado PENDING.")
    public ResponseEntity<Page<School>> getPendingSchools(Pageable pageable) {
        return ResponseEntity.ok(adminService.getPendingSchools(pageable));
    }

    @PatchMapping("/schools/{schoolId}/process")
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

    @PatchMapping("/companies/{companyId}/process")
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

    @PatchMapping("/opportunities/{oppId}/process")
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

    @GetMapping("/pending-placements")
    @Operation(summary = "Listar Placements que esperan aprobación administrativa (Stage 4)")
    public ResponseEntity<Page<Application>> getPendingPlacements(Pageable pageable) {
        return ResponseEntity.ok(adminService.getPendingPlacements(pageable));
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

    // ── GESTIÓN GLOBAL DE USUARIOS (KICK / VIEW ALL) ── //

    @GetMapping("/all-schools")
    @Operation(summary = "Listar todas las escuelas registradas.")
    public ResponseEntity<Page<School>> getAllSchools(Pageable pageable) {
        return ResponseEntity.ok(adminService.getAllSchools(pageable));
    }

    @GetMapping("/all-companies")
    @Operation(summary = "Listar todas las empresas registradas.")
    public ResponseEntity<Page<Company>> getAllCompanies(Pageable pageable) {
        return ResponseEntity.ok(adminService.getAllCompanies(pageable));
    }

    @GetMapping("/all-students")
    @Operation(summary = "Listar todos los estudiantes del sistema.")
    public ResponseEntity<Page<Student>> getAllStudents(Pageable pageable) {
        return ResponseEntity.ok(adminService.getAllStudents(pageable));
    }

    @PatchMapping("/users/{userId}/status")
    @Operation(summary = "Suspender o Activar a cualquier usuario del sistema.")
    public ResponseEntity<User> changeUserStatus(@PathVariable Long userId, @RequestParam User.UserStatus status) {
        return ResponseEntity.ok(adminService.changeUserStatus(userId, status));
    }

    @DeleteMapping("/users/{userId}")
    @Operation(summary = "Eliminar un usuario y su ficha asociada (Cuidado: Cascada completa).")
    public ResponseEntity<Void> deleteUser(@PathVariable Long userId) {
        adminService.deleteUser(userId);
        return ResponseEntity.noContent().build();
    }

    @PostMapping("/students/invite")
    @Operation(summary = "ADMIN INVITA A UN ALUMNO: Debe especificar la escuela.")
    public ResponseEntity<Student> inviteStudent(
        @RequestParam Long schoolId,
        @RequestParam String firstName,
        @RequestParam String lastName,
        @RequestParam String email) {
        return ResponseEntity.ok(adminService.inviteStudent(schoolId, firstName, lastName, email));
    }

    @PostMapping("/students/import-csv")
    @Operation(summary = "ADMIN IMPORTA ESTUDIANTES POR CSV: Debe especificar la escuela.")
    public ResponseEntity<List<Student>> importStudents(
        @RequestParam Long schoolId,
        @RequestPart("file") MultipartFile file) {
        return ResponseEntity.ok(adminService.importStudentsByCsv(schoolId, file));
    }
}
