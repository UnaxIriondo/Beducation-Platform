package com.beducation.model;

import jakarta.persistence.*;
import lombok.*;
import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

/**
 * ============================================================
 * Entidad: Company
 * ============================================================
 * Representa una empresa que publica ofertas de prácticas
 * internacionales en la plataforma BeDucation.
 *
 * Las empresas deben registrarse y esperar aprobación del admin.
 * Una vez aprobadas, pueden:
 *   - Publicar ofertas de prácticas (con revisión del admin)
 *   - Revisar candidatos que aplican a sus ofertas
 *   - Gestionar el proceso de selección (5 etapas del workflow)
 *
 * El logo se almacena en AWS S3, solo se guarda la clave S3.
 * ============================================================
 */
@Entity
@Table(name = "companies", indexes = {
    @Index(name = "idx_company_user", columnList = "user_id"),
    @Index(name = "idx_company_status", columnList = "status")
})
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@JsonIgnoreProperties({"hibernateLazyInitializer", "handler"})
public class Company {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    /**
     * Usuario autenticado que representa a esta empresa.
     * Relación 1:1 — cada cuenta es una sola empresa.
     */
    @OneToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false, unique = true)
    private User user;

    /** Nombre oficial de la empresa. */
    @Column(nullable = false)
    private String name;

    /** País donde está registrada la empresa. */
    @Column(nullable = false)
    private String country;

    /** Ciudad de la sede/oficina. */
    private String city;

    /** Sitio web corporativo. */
    private String website;

    /** Descripción breve de la empresa (sector, actividad, etc.). */
    @Column(columnDefinition = "TEXT")
    private String description;

    /** Tamaño de la empresa (número de empleados, aproximado). */
    @Column(name = "company_size")
    private String companySize;

    /** Sector de actividad (tecnología, salud, educación, etc.). */
    private String sector;

    /**
     * Clave del objeto en AWS S3 donde está el logo de la empresa.
     * Para obtener la URL pública: s3://bucketName/logoS3Key
     */
    @Column(name = "logo_s3_key")
    private String logoS3Key;

    /**
     * Estado de la solicitud de registro de la empresa.
     * Usa el mismo enum de School para consistencia: PENDING/APPROVED/REJECTED
     */
    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    @Builder.Default
    private School.ApprovalStatus status = School.ApprovalStatus.PENDING;

    /** Motivo de rechazo por parte del administrador. */
    @Column(name = "rejection_reason", columnDefinition = "TEXT")
    private String rejectionReason;

    /** Fecha de aprobación del registro de la empresa. */
    @Column(name = "approved_at")
    private LocalDateTime approvedAt;

    /**
     * Ofertas publicadas por esta empresa.
     * Relación 1:N — una empresa puede tener múltiples ofertas.
     */
    @OneToMany(mappedBy = "company", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    @Builder.Default
    @JsonIgnore
    private List<Opportunity> opportunities = new ArrayList<>();

    @CreationTimestamp
    @Column(name = "created_at", updatable = false)
    private LocalDateTime createdAt;

    @UpdateTimestamp
    @Column(name = "updated_at")
    private LocalDateTime updatedAt;
}
