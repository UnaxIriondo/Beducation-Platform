package com.beducation.model;

import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;

/**
 * ============================================================
 * Entidad: Interview
 * ============================================================
 * Almacena los datos de una entrevista programada entre la
 * empresa y el estudiante (Etapa 2 del workflow).
 *
 * La empresa introduce una fecha/hora y un enlace de videollamada
 * (Google Meet, Zoom, Teams, etc.). El estudiante recibe un
 * email de notificación via AWS SES.
 * ============================================================
 */
@Entity
@Table(name = "interviews")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Interview {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    /**
     * Solicitud a la que pertenece esta entrevista.
     * Relación 1:1 — una solicitud tiene como máximo una entrevista.
     */
    @OneToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "application_id", nullable = false, unique = true)
    private Application application;

    /** Fecha y hora programada para la entrevista. */
    @Column(name = "scheduled_at", nullable = false)
    private LocalDateTime scheduledAt;

    /**
     * Enlace de la videollamada (Google Meet, Zoom, Teams, etc.).
     * Compartido con el estudiante por email y visible en su portal.
     */
    @Column(name = "video_call_link")
    private String videoCallLink;

    /** Notas adicionales de la empresa para el estudiante. */
    @Column(columnDefinition = "TEXT")
    private String notes;

    @Column(name = "created_at", updatable = false)
    private LocalDateTime createdAt;
}
