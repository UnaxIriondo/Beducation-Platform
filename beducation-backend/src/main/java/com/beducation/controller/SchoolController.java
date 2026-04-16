package com.beducation.controller;

import com.beducation.dto.SchoolRegistrationDto;
import com.beducation.dto.SchoolProfileDto;
import com.beducation.model.Application;
import com.beducation.model.School;
import com.beducation.model.Student;
import com.beducation.model.User;
import com.beducation.service.SchoolService;
import com.beducation.security.SecurityUtils;
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

import java.util.List;

/**
 * ============================================================
 * Controlador: SchoolController
 * ============================================================
 * Endpoints REST del Portal de Escuelas.
 * ============================================================
 */
@RestController
@RequestMapping("/schools")
@RequiredArgsConstructor
@Tag(name = "School Portal", description = "Endpoints para registro y operaciones de las Escuelas")
public class SchoolController {

    private final SchoolService schoolService;
    private final SecurityUtils securityUtils;

    @PostMapping
    @Operation(summary = "Registrar nueva escuela", description = "Endpoint público para registro inicial. Queda PENDING.")
    public ResponseEntity<School> registerSchool(@RequestBody @Valid SchoolRegistrationDto dto) {
        School school = schoolService.registerSchool(dto);
        return new ResponseEntity<>(school, HttpStatus.CREATED);
    }

    @PostMapping("/{schoolId}/invite-student")
    @PreAuthorize("hasAuthority('SCOPE_SCHOOL')")
    @Operation(summary = "Invitar un alumno (1 a 1)", security = @SecurityRequirement(name = "bearerAuth"))
    public ResponseEntity<Student> inviteStudent(
        @PathVariable Long schoolId,
        @RequestParam String firstName,
        @RequestParam String lastName,
        @RequestParam String email) {
        
        Student student = schoolService.inviteStudent(schoolId, firstName, lastName, email);
        return ResponseEntity.ok(student);
    }

    @GetMapping("/{schoolId}/students")
    @PreAuthorize("hasAuthority('SCOPE_SCHOOL') or hasAuthority('SCOPE_ADMIN')")
    @Operation(summary = "Dashboard de alumnos de la escuela", security = @SecurityRequirement(name = "bearerAuth"))
    public ResponseEntity<Page<Student>> getStudents(
        @PathVariable Long schoolId,
        Pageable pageable) {
        return ResponseEntity.ok(schoolService.getSchoolStudents(schoolId, pageable));
    }

    @GetMapping("/me")
    @PreAuthorize("hasAuthority('SCOPE_SCHOOL')")
    @Operation(summary = "Obtener perfil de la escuela logueada", security = @SecurityRequirement(name = "bearerAuth"))
    public ResponseEntity<SchoolProfileDto> getMyProfile() {
        User user = securityUtils.getCurrentUser();
        School school = schoolService.getSchoolByUserId(user.getId());
        return ResponseEntity.ok(SchoolProfileDto.fromEntity(school));
    }

    @PutMapping("/{schoolId}")
    @PreAuthorize("hasAuthority('SCOPE_SCHOOL') or hasAuthority('SCOPE_ADMIN')")
    @Operation(summary = "Actualizar perfil de la escuela", security = @SecurityRequirement(name = "bearerAuth"))
    public ResponseEntity<School> updateProfile(
        @PathVariable Long schoolId,
        @RequestBody @Valid SchoolProfileDto dto) {
        
        // Verificación de seguridad básica: la escuela solo puede editarse a sí misma 
        // (a menos que sea ADMIN, pero aquí simplificamos con la protección del servicio si fuera necesario)
        User currentUser = securityUtils.getCurrentUser();
        School currentSchool = schoolService.getSchoolByUserId(currentUser.getId());
        
        if (!currentUser.getRole().equals(User.Role.ADMIN) && !currentSchool.getId().equals(schoolId)) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN).build();
        }

        School updated = schoolService.updateSchoolProfile(schoolId, dto);
        return ResponseEntity.ok(updated);
    }

    @GetMapping("/{schoolId}/pending-applications")
    @PreAuthorize("hasAuthority('SCOPE_SCHOOL')")
    @Operation(summary = "Obtener aplicaciones pendientes de validación", security = @SecurityRequirement(name = "bearerAuth"))
    public ResponseEntity<Page<Application>> getPendingApplications(
        @PathVariable Long schoolId,
        Pageable pageable) {
        return ResponseEntity.ok(schoolService.getPendingApplications(schoolId, pageable));
    }

    @GetMapping("/{schoolId}/stats")
    @PreAuthorize("hasAuthority('SCOPE_SCHOOL')")
    @Operation(summary = "Resumen de Funnel para la Escuela", security = @SecurityRequirement(name = "bearerAuth"))
    public ResponseEntity<java.util.Map<String, Object>> getStats(@PathVariable Long schoolId) {
        return ResponseEntity.ok(schoolService.getDashboardStats(schoolId));
    }
}
