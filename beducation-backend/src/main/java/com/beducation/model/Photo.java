package com.beducation.model;

import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;
import java.time.LocalDateTime;

/**
 * ============================================================
 * Entidad: Photo
 * ============================================================
 * Representa una imagen en la galería de eventos de la plataforma.
 * Las fotos son subidas exclusivamente por el Administrador.
 * ============================================================
 */
@Entity
@Table(name = "photos")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Photo {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    /**
     * Clave (path) del archivo en el bucket de S3.
     */
    @Column(name = "s3_key", nullable = false)
    private String s3Key;

    /**
     * Descripción opcional de la fotografía.
     */
    @Column(columnDefinition = "TEXT")
    private String description;

    /**
     * Fecha y hora en la que se subió la foto.
     */
    @CreationTimestamp
    @Column(name = "uploaded_at", updatable = false)
    private LocalDateTime uploadedAt;

    /**
     * URL completa calculada (opcional, si se usa CloudFront o similar).
     * Si no, se puede construir dinámicamente en el frontend.
     */
}
