package com.beducation.model;

import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import java.time.LocalDateTime;

/**
 * ============================================================
 * Entidad: Application
 * ============================================================
 * Núcleo del sistema BeDucation: representa una solicitud de
 * un estudiante a una oferta de prácticas.
 *
 * FLUJO DE 5 ETAPAS:
 * ─────────────────────────────────────────────────────────────
 * STAGE 1: Estudiante envía solicitud → status APPLIED
 * STAGE 2: Empresa expresa interés → status INTERESTED
 *          → Empresa programa entrevista (Interview separado)
 * STAGE 3: Empresa hace oferta formal → status OFFERED
 *          → Estudiante puede aceptar o rechazar
 * STAGE 4: Admin aprueba el placement → status ADMIN_APPROVED
 * STAGE 5: Escuela da aprobación final → status CONFIRMED
 *          → Otras solicitudes del estudiante se cancelan
 *
 * ESTADOS FINALES:
 *   REJECTED     → Empresa rechazó al candidato
 *   WITHDRAWN    → Estudiante retiró la solicitud
 *   EXPIRED      → Sin actividad durante 30 días
 *   OFFER_REJECTED → Estudiante rechazó la oferta de la empresa
 * ─────────────────────────────────────────────────────────────
 */
@Entity
@Table(name = "applications", indexes = {
    @Index(name = "idx_app_student", columnList = "student_id"),
    @Index(name = "idx_app_opportunity", columnList = "opportunity_id"),
    @Index(name = "idx_app_status", columnList = "status"),
    @Index(name = "idx_app_stage", columnList = "stage")
})
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Application {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    /**
     * Estudiante que realizó la solicitud.
     * Un estudiante puede tener múltiples solicitudes activas.
     */
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "student_id", nullable = false)
    private Student student;

    /**
     * Oferta de prácticas a la que aplica el estudiante.
     */
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "opportunity_id", nullable = false)
    private Opportunity opportunity;

    /**
     * Etapa actual del flujo de trabajo (1-5).
     * Representa el progreso en el proceso de selección.
     */
    @Column(nullable = false)
    @Builder.Default
    private Integer stage = 1;

    /**
     * Estado detallado de la solicitud.
     * Más granular que el número de etapa.
     */
    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    @Builder.Default
    private ApplicationStatus status = ApplicationStatus.APPLIED;

    /**
     * Motivo de rechazo (si la empresa o el admin rechazó).
     * Obligatorio cuando status = REJECTED.
     */
    @Column(name = "rejection_reason", columnDefinition = "TEXT")
    private String rejectionReason;

    /**
     * Nota interna del proceso (observaciones del admin, etc.).
     */
    @Column(columnDefinition = "TEXT")
    private String notes;

    /**
     * Fecha límite de actividad antes de marcar como EXPIRED.
     * Se renueva con cada cambio de estado.
     * La lógica de expiración (30 días) se ejecuta en un scheduler.
     */
    @Column(name = "expires_at")
    private LocalDateTime expiresAt;

    /** Fecha en que el estudiante envió la solicitud. */
    @Column(name = "applied_at")
    private LocalDateTime appliedAt;

    /** Fecha de la última actualización de estado. */
    @Column(name = "status_updated_at")
    private LocalDateTime statusUpdatedAt;

    /**
     * Datos de la entrevista programada (opcional, en etapa 2).
     * Relación 1:1 — una solicitud puede tener una entrevista.
     */
    @OneToOne(mappedBy = "application", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    private Interview interview;

    @CreationTimestamp
    @Column(name = "created_at", updatable = false)
    private LocalDateTime createdAt;

    @UpdateTimestamp
    @Column(name = "updated_at")
    private LocalDateTime updatedAt;

    /**
     * Estados detallados del ciclo de vida de una solicitud.
     * Cada estado corresponde a una etapa del flujo de 5 pasos.
     */
    public enum ApplicationStatus {
        // ── Etapa 1: Solicitud enviada
        APPLIED,

        // ── Etapa 2: Empresa interesada
        INTERESTED,

        // ── Etapa 3: Oferta formal realizada
        OFFERED,

        // ── Etapa 4: Admin ha pre-aprobado el placement
        ADMIN_APPROVED,

        // ── Etapa 5: Escuela ha confirmado el placement
        CONFIRMED,

        // ── Estados finales negativos
        REJECTED,        // Empresa rechazó al candidato
        WITHDRAWN,       // Estudiante retiró la solicitud
        EXPIRED,         // Sin actividad por 30 días
        OFFER_REJECTED   // Estudiante rechazó la oferta
    }
}
