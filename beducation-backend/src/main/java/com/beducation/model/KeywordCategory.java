package com.beducation.model;

import jakarta.persistence.*;
import lombok.*;

import java.util.ArrayList;
import java.util.List;

/**
 * ============================================================
 * Entidad: KeywordCategory
 * ============================================================
 * Agrupa keywords por área temática para facilitar la selección
 * en la interfaz de usuario.
 *
 * Ejemplos de categorías:
 *   - Tecnología (Java, Python, React, Docker...)
 *   - Marketing (SEO, SEM, Redes Sociales...)
 *   - Diseño (Figma, Photoshop, UI/UX...)
 *   - Idiomas (Inglés, Francés, Alemán...)
 *
 * Gestionadas exclusivamente por el Admin de la plataforma.
 * ============================================================
 */
@Entity
@Table(name = "keyword_categories")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class KeywordCategory {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    /** Nombre de la categoría (debe ser único). */
    @Column(nullable = false, unique = true)
    private String name;

    /**
     * Keywords que pertenecen a esta categoría.
     * Cuando se elimina la categoría, las keywords se eliminan también.
     */
    @OneToMany(mappedBy = "category", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    @Builder.Default
    private List<Keyword> keywords = new ArrayList<>();
}
