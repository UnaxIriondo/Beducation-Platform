package com.beducation.model;

import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

/**
 * ============================================================
 * Entidad: School
 * ============================================================
 * Representa una institución educativa registrada en BeDucation.
 * Las escuelas son responsables de:
 *   - Invitar a sus estudiantes a la plataforma
 *   - Dar la aprobación final (Etapa 5) a los placements
 *
 * Flujo de registro:
 *   PENDING → (admin aprueba) → APPROVED
 *   PENDING → (admin rechaza) → REJECTED
 * ============================================================
 */
@Entity
@Table(name = "schools", indexes = {
    @Index(name = "idx_school_user", columnList = "user_id"),
    @Index(name = "idx_school_status", columnList = "status")
})
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class School {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    /**
     * Usuario autenticado asociado a esta escuela.
     * Relación 1:1 — cada cuenta de usuario es una sola escuela.
     */
    @OneToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false, unique = true)
    private User user;

    /** Nombre oficial de la institución educativa. */
    @Column(nullable = false)
    private String name;

    /** País donde está ubicada la escuela. */
    @Column(nullable = false)
    private String country;

    /** Ciudad de la escuela. */
    private String city;

    /** Dirección postal completa. */
    private String address;

    /** Sitio web oficial de la escuela. */
    private String website;

    /** Nombre del responsable de contacto en la escuela. */
    @Column(name = "contact_person")
    private String contactPerson;

    /** Teléfono de contacto. */
    private String phone;

    /**
     * Estado de la solicitud de registro.
     * PENDING: esperando aprobación del admin.
     * APPROVED: escuela activa en la plataforma.
     * REJECTED: solicitud denegada por el admin.
     */
    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    @Builder.Default
    private ApprovalStatus status = ApprovalStatus.PENDING;

    /** Motivo de rechazo (si el admin rechazó el registro). */
    @Column(name = "rejection_reason", columnDefinition = "TEXT")
    private String rejectionReason;

    /** Fecha/hora de aprobación por parte del admin. */
    @Column(name = "approved_at")
    private LocalDateTime approvedAt;

    /**
     * Lista de estudiantes invitados/pertenecientes a esta escuela.
     * Relación 1:N — una escuela tiene múltiples estudiantes.
     */
    @OneToMany(mappedBy = "school", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    @Builder.Default
    private List<Student> students = new ArrayList<>();

    @CreationTimestamp
    @Column(name = "created_at", updatable = false)
    private LocalDateTime createdAt;

    @UpdateTimestamp
    @Column(name = "updated_at")
    private LocalDateTime updatedAt;

    /**
     * Estados de aprobación compartidos por escuelas, empresas y ofertas.
     */
    public enum ApprovalStatus {
        PENDING, APPROVED, REJECTED
    }
}
