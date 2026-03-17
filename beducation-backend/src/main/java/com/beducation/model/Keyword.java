package com.beducation.model;

import jakarta.persistence.*;
import lombok.*;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

import java.util.ArrayList;
import java.util.List;

/**
 * ============================================================
 * Entidad: Keyword
 * ============================================================
 * Representa una habilidad, tecnología o área de conocimiento.
 * Ejemplos: "Java", "Marketing Digital", "Diseño Gráfico"
 *
 * Las keywords están organizadas en categorías (ej. "Tecnología",
 * "Negocios", "Diseño") gestionadas por el admin.
 *
 * USO EN EL ALGORITMO DE MATCHING:
 *   - 70% del peso del score de compatibilidad
 *   - Se compara el conjunto de keywords del estudiante con
 *     el conjunto de keywords requeridas en la oferta
 * ============================================================
 */
@Entity
@Table(name = "keywords", indexes = {
    @Index(name = "idx_keyword_category", columnList = "category_id")
})
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@JsonIgnoreProperties({"hibernateLazyInitializer", "handler"})
public class Keyword {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    /** Nombre de la keyword (único dentro de su categoría). */
    @Column(nullable = false, unique = true)
    private String name;

    /**
     * Categoría a la que pertenece esta keyword.
     * Ayuda a organizar la selección en la UI (selector agrupado).
     */
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "category_id", nullable = false)
    private KeywordCategory category;
}
