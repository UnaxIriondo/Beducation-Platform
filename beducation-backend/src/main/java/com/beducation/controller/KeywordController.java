package com.beducation.controller;

import com.beducation.model.Keyword;
import com.beducation.repository.KeywordRepository;
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
 * Controlador: KeywordController
 * ============================================================
 * Expone el diccionario de habilidades/keywords para que los
 * estudiantes puedan seleccionarlas en su perfil.
 * ============================================================
 */
@RestController
@RequestMapping("/keywords")
@RequiredArgsConstructor
@Tag(name = "Keywords", description = "Diccionario de habilidades y competencias")
public class KeywordController {

    private final KeywordRepository keywordRepository;

    @GetMapping
    @Operation(summary = "Listar todas las keywords disponibles (ordenadas por categoría)")
    public ResponseEntity<List<Keyword>> getAllKeywords() {
        return ResponseEntity.ok(keywordRepository.findAllByOrderByCategoryNameAscNameAsc());
    }
}
