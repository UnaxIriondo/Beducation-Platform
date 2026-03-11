package com.beducation.controller;

import com.beducation.dto.SchoolRegistrationDto;
import com.beducation.model.School;
import com.beducation.model.Student;
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
    @PreAuthorize("hasAuthority('SCOPE_SCHOOL')") // o hasRole('SCHOOL') dependiendo configuración Auth0
    @Operation(summary = "Invitar un alumno (1 a 1)", security = @SecurityRequirement(name = "bearerAuth"))
    public ResponseEntity<Student> inviteStudent(
        @PathVariable Long schoolId,
        @RequestParam String firstName,
        @RequestParam String lastName,
        @RequestParam String email) {
        
        Student student = schoolService.inviteStudent(schoolId, firstName, lastName, email);
        return ResponseEntity.ok(student);
    }

    @PostMapping("/{schoolId}/import-students")
    @PreAuthorize("hasAuthority('SCOPE_SCHOOL')")
    @Operation(summary = "Importación CSV Masiva (Máx 100 alumnos)", security = @SecurityRequirement(name = "bearerAuth"))
    public ResponseEntity<List<Student>> importStudents(
        @PathVariable Long schoolId,
        @RequestPart("file") MultipartFile file) {
        
        List<Student> students = schoolService.importStudentsByCsv(schoolId, file);
        return ResponseEntity.ok(students);
    }

    @GetMapping("/{schoolId}/students")
    @PreAuthorize("hasAuthority('SCOPE_SCHOOL') or hasAuthority('SCOPE_ADMIN')")
    @Operation(summary = "Dashboard de alumnos de la escuela", security = @SecurityRequirement(name = "bearerAuth"))
    public ResponseEntity<Page<Student>> getStudents(
        @PathVariable Long schoolId,
        Pageable pageable) {
        return ResponseEntity.ok(schoolService.getSchoolStudents(schoolId, pageable));
    }
}
