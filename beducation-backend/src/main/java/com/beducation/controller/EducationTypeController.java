package com.beducation.controller;

import com.beducation.model.EducationType;
import com.beducation.repository.EducationTypeRepository;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

/**
 * ============================================================
 * Controlador: EducationTypeController
 * ============================================================
 * Expone los tipos de educación (FP, Universidad, etc.) para
 * que puedan ser seleccionados en perfiles y vacantes.
 * ============================================================
 */
@RestController
@RequestMapping("/education-types")
@RequiredArgsConstructor
@Tag(name = "Education Types", description = "Catálogo de tipos de formación homologados")
public class EducationTypeController {

    private final EducationTypeRepository educationTypeRepository;

    @GetMapping
    @Operation(summary = "Listar todos los tipos de educación disponibles")
    public ResponseEntity<List<EducationType>> getAll() {
        return ResponseEntity.ok(educationTypeRepository.findAll());
    }
}
