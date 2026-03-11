package com.beducation.controller;

import com.beducation.dto.StudentProfileDto;
import com.beducation.model.Student;
import com.beducation.model.StudentDocument;
import com.beducation.service.StudentService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

/**
 * ============================================================
 * Controlador: StudentController
 * ============================================================
 * Endpoints REST del Portal de Estudiante.
 * ============================================================
 */
@RestController
@RequestMapping("/students")
@RequiredArgsConstructor
@Tag(name = "Student Portal", description = "Endpoints para el onboarding, perfil de alumno y subida a AWS. Aplicaciones y matching van aparte.")
public class StudentController {

    private final StudentService studentService;

    @PutMapping("/{studentId}/profile")
    @PreAuthorize("hasAuthority('SCOPE_STUDENT')")
    @Operation(summary = "Actualizar perfil/onboarding y requerimientos para aplicar.", security = @SecurityRequirement(name = "bearerAuth"))
    public ResponseEntity<Student> updateProfile(
        @PathVariable Long studentId,
        @RequestBody StudentProfileDto dto) {
        
        Student updated = studentService.updateProfile(studentId, dto);
        return ResponseEntity.ok(updated);
    }

    @PostMapping("/{studentId}/documents")
    @PreAuthorize("hasAuthority('SCOPE_STUDENT')")
    @Operation(summary = "Subir documento AWS S3 (CV, Foto, Carta)", security = @SecurityRequirement(name = "bearerAuth"))
    public ResponseEntity<StudentDocument> uploadDocument(
        @PathVariable Long studentId,
        @RequestParam("type") StudentDocument.DocumentType type,
        @RequestPart("file") MultipartFile file) {
        
        StudentDocument document = studentService.uploadDocument(studentId, type, file);
        return ResponseEntity.ok(document);
    }

    @GetMapping("/{studentId}")
    @PreAuthorize("hasAuthority('SCOPE_STUDENT') or hasAuthority('SCOPE_COMPANY')")
    @Operation(summary = "Obtener perfil público/interno del alumno", security = @SecurityRequirement(name = "bearerAuth"))
    public ResponseEntity<Student> getProfile(@PathVariable Long studentId) {
        return ResponseEntity.ok(studentService.getStudentProfile(studentId));
    }
}
