package com.beducation.controller;

import com.beducation.model.Opportunity;
import com.beducation.model.Student;
import com.beducation.repository.OpportunityRepository;
import com.beducation.repository.StudentRepository;
import com.beducation.util.MatchingEngine;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * ============================================================
 * Controlador: OpportunitySearchController & Matching
 * ============================================================
 * Endpoints usados fundamentalmente por el estudiante para buscar 
 * (en tableros) o ser emparejado automáticamente por algoritmos a las ofertas publicadas.
 * ============================================================
 */
@RestController
@RequestMapping("/opportunities")
@RequiredArgsConstructor
@Tag(name = "Search Opportunities & Matches", description = "Motor de búsqueda manual y MatchMaker (70%-20%-10%)")
public class OpportunitySearchController {

    private final OpportunityRepository opportunityRepository;
    private final StudentRepository studentRepository;
    private final MatchingEngine matchingEngine;

    // ── BUSCADOR DE OFERTAS PUBLICADAS ── //

    @GetMapping("/search")
    @PreAuthorize("isAuthenticated()") // Requiere login
    @Operation(summary = "Dashboard de Candidato. Busca usando filtros libres.", security = @SecurityRequirement(name = "bearerAuth"))
    public ResponseEntity<Page<Opportunity>> searchApproved(
        @RequestParam(required = false) String country,
        @RequestParam(required = false) Long educTypeId,
        @RequestParam(required = false) String keyword,
        Pageable pageable) {

        Page<Opportunity> results = opportunityRepository.searchApprovedOpportunities(
            country, educTypeId, keyword, pageable);
        return ResponseEntity.ok(results);
    }

    @GetMapping("/{id}")
    @PreAuthorize("isAuthenticated()")
    @Operation(summary = "Obtener detalles de una oferta específica.", security = @SecurityRequirement(name = "bearerAuth"))
    public ResponseEntity<Opportunity> getById(@PathVariable Long id) {
        return opportunityRepository.findById(id)
            .map(ResponseEntity::ok)
            .orElse(ResponseEntity.notFound().build());
    }

    // ── MATCHING ALGORITHM (SUGERERENCIAS DE ALTA INTELIGENCIA) ── //

    @GetMapping("/matching/student/{studentId}")
    @PreAuthorize("hasAuthority('SCOPE_STUDENT')")
    @Operation(summary = "Sugerencia Inteligente post Onboarding o continua. Analiza % Jaccard compatibilidad.", security = @SecurityRequirement(name = "bearerAuth"))
    public ResponseEntity<List<MatchingEngine.MatchResult>> getSuggeredMatches(
        @PathVariable Long studentId,
        @RequestParam(defaultValue = "10") int limit) {

        Student student = studentRepository.findById(studentId)
            .orElseThrow(() -> new RuntimeException("Estudiante no existe"));
            
        // Si el estudiante no ha completado el perfil o no tiene skills base 
        // arrojará unas matches menores, por lo cual conviene validarlo en Frontend como un bloqueo modal
        List<MatchingEngine.MatchResult> topMatches = matchingEngine.findBestMatches(student, limit);
        return ResponseEntity.ok(topMatches);
    }
}
