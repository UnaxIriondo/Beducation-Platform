package com.beducation.model;

import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;
import java.time.LocalDateTime;

/**
 * ============================================================
 * Entidad: GalleryAccessRequest
 * ============================================================
 * Gestiona los permisos de acceso a la galería de fotos.
 * Estudiantes y Empresas solicitan acceso, y el Admin lo concede.
 * ============================================================
 */
@Entity
@Table(name = "gallery_access_requests", indexes = {
    @Index(name = "idx_gallery_user", columnList = "user_id"),
    @Index(name = "idx_gallery_status", columnList = "status")
})
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class GalleryAccessRequest {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    /**
     * Usuario que solicita el acceso (Estudiante o Empresa).
     */
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    /**
     * Estado de la solicitud.
     */
    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    @Builder.Default
    private AccessStatus status = AccessStatus.PENDING;

    /**
     * Fecha en la que se realizó la solicitud.
     */
    @CreationTimestamp
    @Column(name = "requested_at", updatable = false)
    private LocalDateTime requestedAt;

    /**
     * Fecha en la que el administrador respondió a la solicitud.
     */
    @Column(name = "responded_at")
    private LocalDateTime respondedAt;

    /**
     * Motivo opcional (para rechazos).
     */
    @Column(name = "rejection_reason")
    private String rejectionReason;

    public enum AccessStatus {
        PENDING, APPROVED, REJECTED
    }
}
