package com.beducation.model;

import jakarta.persistence.*;
import lombok.*;
import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

/**
 * ============================================================
 * Entidad: Opportunity
 * ============================================================
 * Representa una oferta de prácticas publicada por una empresa.
 *
 * Ciclo de vida de una oferta:
 *   DRAFT         → La empresa la está editando (no visible)
 *   PENDING_REVIEW → Enviada al admin para revisión
 *   APPROVED      → Aprobada, visible para estudiantes
 *   CLOSED        → Plazas cubiertas o fecha expirada
 *
 * El matching del algoritmo considera:
 *   - Keywords de la oferta vs. keywords del estudiante (70%)
 *   - País de la oferta vs. preferencias del estudiante (20%)
 *   - Tipo de formación requerido vs. tipo del estudiante (10%)
 * ============================================================
 */
@Entity
@Table(name = "opportunities", indexes = {
    @Index(name = "idx_opp_company", columnList = "company_id"),
    @Index(name = "idx_opp_status", columnList = "status"),
    @Index(name = "idx_opp_edu_type", columnList = "education_type_id"),
    @Index(name = "idx_opp_country", columnList = "country")
})
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@JsonIgnoreProperties({"hibernateLazyInitializer", "handler"})
public class Opportunity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    /**
     * Empresa que publica esta oferta.
     * Relación N:1 — una empresa puede tener múltiples ofertas.
     */
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "company_id", nullable = false)
    private Company company;

    /** Título de la oferta (ej. "Prácticas en Desarrollo Web"). */
    @Column(nullable = false)
    private String title;

    /** Descripción detallada de las tareas y responsabilidades. */
    @Column(columnDefinition = "TEXT", nullable = false)
    private String description;

    /** País donde se realizan las prácticas. */
    @Column(nullable = false)
    private String country;

    /** Ciudad donde se realizan las prácticas. */
    private String city;

    /** Fecha de inicio de las prácticas. */
    @Column(name = "start_date")
    private LocalDate startDate;

    /** Fecha de fin de las prácticas. */
    @Column(name = "end_date")
    private LocalDate endDate;

    /**
     * Número de plazas disponibles.
     * Cuando llega a 0, la oferta se marca CLOSED automáticamente.
     */
    @Column(nullable = false)
    @Builder.Default
    private Integer spots = 1;

    /**
     * Plazas actualmente disponibles (decrece con confirmaciones).
     */
    @Column(name = "spots_available", nullable = false)
    @Builder.Default
    private Integer spotsAvailable = 1;

    /**
     * Tipo de formación requerido para aplicar a esta oferta.
     * El estudiante debe tener el mismo tipo para que haya match.
     */
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "education_type_id")
    private EducationType educationType;

    /**
     * Estado de la oferta en su ciclo de vida.
     * Solo las ofertas APPROVED son visibles para los estudiantes.
     */
    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    @Builder.Default
    private OpportunityStatus status = OpportunityStatus.DRAFT;

    /** Motivo de rechazo por parte del administrador. */
    @Column(name = "rejection_reason", columnDefinition = "TEXT")
    private String rejectionReason;

    /** Fecha en que el admin aprobó la oferta. */
    @Column(name = "approved_at")
    private LocalDateTime approvedAt;

    /**
     * Keywords de habilidades/áreas requeridas para esta oferta.
     * Usadas en el algoritmo de matching con los estudiantes.
     */
    @ManyToMany(fetch = FetchType.LAZY)
    @JoinTable(
        name = "opportunity_keywords",
        joinColumns = @JoinColumn(name = "opportunity_id"),
        inverseJoinColumns = @JoinColumn(name = "keyword_id")
    )
    @Builder.Default
    private List<Keyword> keywords = new ArrayList<>();

    /**
     * Solicitudes recibidas para esta oferta.
     */
    @OneToMany(mappedBy = "opportunity", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    @Builder.Default
    @JsonIgnore
    private List<Application> applications = new ArrayList<>();

    @CreationTimestamp
    @Column(name = "created_at", updatable = false)
    private LocalDateTime createdAt;

    @UpdateTimestamp
    @Column(name = "updated_at")
    private LocalDateTime updatedAt;

    /**
     * Estados del ciclo de vida de una oferta.
     */
    public enum OpportunityStatus {
        DRAFT,           // Borrador — solo visible para la empresa
        PENDING_REVIEW,  // Enviada al admin para aprobación
        APPROVED,        // Activa y visible para estudiantes
        CLOSED           // Cerrada (plazas agotadas o expirada)
    }
}
