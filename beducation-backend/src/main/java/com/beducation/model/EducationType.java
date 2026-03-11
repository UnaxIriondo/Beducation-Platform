package com.beducation.model;

import jakarta.persistence.*;
import lombok.*;

/**
 * ============================================================
 * Entidad: EducationType
 * ============================================================
 * Define los tipos de formación académica reconocidos en la
 * plataforma BeDucation, según la nomenclatura española de FP
 * y educación universitaria.
 *
 * Ejemplos:
 *   - FP Básica
 *   - FP Grado Medio (CFGM)
 *   - FP Grado Superior (CFGS)
 *   - Grado Universitario
 *   - Máster Universitario
 *
 * USO EN EL MATCHING:
 *   - 10% del peso del score de compatibilidad
 *   - La oferta tiene un tipo requerido; el estudiante tiene su tipo
 *   - Si coinciden exactamente: contribución máxima al score
 * ============================================================
 */
@Entity
@Table(name = "education_types")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class EducationType {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    /** Nombre completo del tipo de formación. */
    @Column(nullable = false, unique = true)
    private String name;

    /**
     * Código corto para identificación interna y uso en APIs.
     * Ejemplo: "CFGS", "GRADO", "MASTER"
     */
    @Column(nullable = false, unique = true)
    private String code;

    /** Descripción adicional del tipo de formación (opcional). */
    private String description;
}
