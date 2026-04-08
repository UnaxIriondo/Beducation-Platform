package com.beducation.util;

import com.beducation.model.Keyword;
import com.beducation.model.Opportunity;
import com.beducation.model.Student;
import com.beducation.repository.OpportunityRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.util.Comparator;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

/**
 * ============================================================
 * Utilidad: MatchingEngine
 * ============================================================
 * Implementa el algoritmo de compatibilidad entre estudiantes
 * y ofertas de prácticas.
 *
 * FÓRMULA DE PUNTUACIÓN (score total de 0.0 a 1.0):
 * ─────────────────────────────────────────────────────────────
 *   score = (keywordScore × 0.70)
 *         + (locationScore × 0.20)
 *         + (educationScore × 0.10)
 * ─────────────────────────────────────────────────────────────
 *
 * KEYWORD SCORE (70%):
 *   Coeficiente de Jaccard entre los conjuntos de keywords:
 *   |Intersección(student_kw, opportunity_kw)| / |Unión(student_kw, opportunity_kw)|
 *   Rango: 0.0 (sin coincidencias) a 1.0 (perfectamente iguales)
 *
 * LOCATION SCORE (20%):
 *   1.0 → El país de la oferta está en las preferencias del estudiante
 *   0.0 → El país no está entre las preferencias
 *
 * EDUCATION SCORE (10%):
 *   1.0 → El tipo de formación del estudiante == tipo requerido por la oferta
 *   0.5 → La oferta no requiere un tipo específico
 *   0.0 → Tipos incompatibles
 *
 * Las ofertas con score < 0.1 se consideran incompatibles y no se muestran.
 * ─────────────────────────────────────────────────────────────
 */
@Component
@RequiredArgsConstructor
@Slf4j
public class MatchingEngine {

    private final OpportunityRepository opportunityRepository;

    // Pesos del algoritmo de matching (deben sumar 1.0)
    private static final double KEYWORD_WEIGHT  = 0.70;
    private static final double LOCATION_WEIGHT = 0.20;
    private static final double EDUCATION_WEIGHT = 0.10;

    // Umbral mínimo para mostrar una oferta como sugerencia
    private static final double MIN_SCORE_THRESHOLD = 0.10;

    /**
     * Calcula y devuelve las ofertas mejor compatibles con un estudiante.
     * Las ofertas se ordenan por puntuación descendente (mejor match primero).
     *
     * @param student el estudiante para quien calcular matches
     * @param maxResults número máximo de ofertas a devolver
     * @return lista de MatchResult con las mejores ofertas y sus puntuaciones
     */
    public List<MatchResult> findBestMatches(Student student, int maxResults) {
        log.debug("Calculando matches para estudiante ID: {}", student.getId());

        // Obtener todas las ofertas aprobadas con plazas disponibles
        List<Opportunity> availableOpportunities =
            opportunityRepository.findAllApprovedWithSpots();

        // Calcular el score para cada oferta y filtrar por umbral mínimo
        return availableOpportunities.stream()
            .map(opportunity -> {
                List<String> commonKeywords = student.getKeywords().stream()
                    .filter(sk -> opportunity.getKeywords().stream().anyMatch(ok -> ok.getId().equals(sk.getId())))
                    .map(Keyword::getName)
                    .collect(Collectors.toList());
                return new MatchResult(opportunity, calculateScore(student, opportunity), commonKeywords);
            })
            .filter(result -> result.score() >= MIN_SCORE_THRESHOLD)
            .sorted(Comparator.comparingDouble(MatchResult::score).reversed())
            .limit(maxResults)
            .collect(Collectors.toList());
    }

    /**
     * Calcula el score de compatibilidad entre un estudiante y una oferta.
     * Aplica la fórmula ponderada con los tres criterios.
     *
     * @param student     el estudiante
     * @param opportunity la oferta
     * @return score de 0.0 a 1.0
     */
    public double calculateScore(Student student, Opportunity opportunity) {
        double keywordScore  = calculateKeywordScore(student, opportunity);
        double locationScore = calculateLocationScore(student, opportunity);
        double educationScore = calculateEducationScore(student, opportunity);

        double totalScore = (keywordScore  * KEYWORD_WEIGHT)
                          + (locationScore * LOCATION_WEIGHT)
                          + (educationScore * EDUCATION_WEIGHT);

        log.debug("Match estudiante={} oferta={}: keywords={:.2f} location={:.2f} edu={:.2f} total={:.2f}",
            student.getId(), opportunity.getId(), keywordScore, locationScore, educationScore, totalScore);

        return totalScore;
    }

    /**
     * KEYWORD SCORE (70% del total):
     * Calcula la similitud de Jaccard entre las keywords del estudiante
     * y las keywords requeridas por la oferta.
     *
     * Ejemplo:
     *   Estudiante: {Java, Spring, Docker}
     *   Oferta: {Java, Python, Docker}
     *   Intersección: {Java, Docker} → 2 elementos
     *   Unión: {Java, Spring, Docker, Python} → 4 elementos
     *   Score = 2/4 = 0.50
     */
    private double calculateKeywordScore(Student student, Opportunity opportunity) {
        Set<Long> studentKeywordIds = student.getKeywords().stream()
            .map(Keyword::getId)
            .collect(Collectors.toSet());

        Set<Long> opportunityKeywordIds = opportunity.getKeywords().stream()
            .map(Keyword::getId)
            .collect(Collectors.toSet());

        // Sin keywords en ninguno de los dos → no hay match de keywords
        if (studentKeywordIds.isEmpty() && opportunityKeywordIds.isEmpty()) {
            return 0.0;
        }

        // Si la oferta no requiere keywords específicas → score neutro
        if (opportunityKeywordIds.isEmpty()) {
            return 0.5;
        }

        // Intersección: keywords en común
        long intersection = studentKeywordIds.stream()
            .filter(opportunityKeywordIds::contains)
            .count();

        // Unión: total de keywords distintas entre los dos conjuntos
        long union = studentKeywordIds.size() + opportunityKeywordIds.size() - intersection;

        return union > 0 ? (double) intersection / union : 0.0;
    }

    /**
     * LOCATION SCORE (20% del total):
     * Comprueba si el país de la oferta está entre las preferencias del estudiante.
     * Un match exacto de país vale 1.0; fuera de preferencias vale 0.0.
     */
    private double calculateLocationScore(Student student, Opportunity opportunity) {
        if (opportunity.getCountry() == null || student.getCountryPreferences().isEmpty()) {
            return 0.5; // Sin preferencias definidas → score neutro
        }

        boolean countryMatches = student.getCountryPreferences().stream()
            .anyMatch(pref -> pref.equalsIgnoreCase(opportunity.getCountry()));

        return countryMatches ? 1.0 : 0.0;
    }

    /**
     * EDUCATION SCORE (10% del total):
     * Comprueba si el tipo de formación del estudiante es compatible
     * con el tipo de formación requerido por la oferta.
     */
    private double calculateEducationScore(Student student, Opportunity opportunity) {
        // Si la oferta no requiere un tipo específico → score neutro
        if (opportunity.getEducationType() == null) {
            return 0.5;
        }

        // Si el estudiante no tiene tipo definido → penalización
        if (student.getEducationType() == null) {
            return 0.0;
        }

        // Match exacto de tipo de formación
        return student.getEducationType().getId()
            .equals(opportunity.getEducationType().getId()) ? 1.0 : 0.0;
    }

    /**
     * Resultado inmutable de un cálculo de matching.
     * Contiene la oferta y su puntuación de compatibilidad.
     *
     * @param opportunity la oferta evaluada
     * @param score       puntuación de 0.0 a 1.0
     * @param matchedKeywords lista de keywords en común
     */
    public record MatchResult(Opportunity opportunity, double score, List<String> matchedKeywords) {}
}
