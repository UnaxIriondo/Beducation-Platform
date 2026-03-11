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
 * Entidad: Student
 * ============================================================
 * Representa a un estudiante en la plataforma BeDucation.
 *
 * Los estudiantes NO pueden auto-registrarse. Son invitados por
 * su escuela (o por el admin). Cuando aceptan la invitación
 * (mediante Auth0), completan su perfil en pasos.
 *
 * Un perfil se considera COMPLETO cuando tiene:
 *   ✓ CV subido (obligatorio)
 *   ✓ Motivación escrita (min. 100 caracteres)
 *   ✓ Al menos 1 keyword seleccionada
 *   ✓ Al menos 1 país de preferencia
 *   ✓ Tipo de formación seleccionado
 *
 * Solo con perfil completo puede enviar solicitudes.
 * ============================================================
 */
@Entity
@Table(name = "students", indexes = {
    @Index(name = "idx_student_user", columnList = "user_id"),
    @Index(name = "idx_student_school", columnList = "school_id"),
    @Index(name = "idx_student_edu_type", columnList = "education_type_id")
})
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Student {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    /**
     * Usuario autenticado asociado al estudiante.
     * Creado cuando el estudiante acepta la invitación.
     */
    @OneToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", unique = true)
    private User user;

    /**
     * Escuela que invitó al estudiante.
     * Relación N:1 — muchos estudiantes → una escuela.
     */
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "school_id", nullable = false)
    private School school;

    // ──────────────────────────────────────
    // Datos personales (recibidos en la invitación)
    // ──────────────────────────────────────

    @Column(name = "first_name", nullable = false)
    private String firstName;

    @Column(name = "last_name", nullable = false)
    private String lastName;

    /** Email de invitación (puede diferir del email de Auth0). */
    @Column(name = "invitation_email", nullable = false, unique = true)
    private String invitationEmail;

    /** Teléfono de contacto (completado en el onboarding). */
    private String phone;

    // ──────────────────────────────────────
    // Perfil académico y motivación
    // ──────────────────────────────────────

    /**
     * Texto de motivación del estudiante para prácticas internacionales.
     * Obligatorio para perfil completo (mínimo 100 caracteres).
     */
    @Column(columnDefinition = "TEXT")
    private String motivation;

    /**
     * Tipo de formación del estudiante (FP, Grado Universitario, etc.).
     * Determina qué ofertas son compatibles con su perfil.
     */
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "education_type_id")
    private EducationType educationType;

    // ──────────────────────────────────────
    // Estado del perfil
    // ──────────────────────────────────────

    /**
     * Indica si el perfil está completo.
     * Se recalcula automáticamente en el servicio cada vez que
     * el estudiante actualiza su perfil o sube documentos.
     */
    @Column(name = "profile_complete", nullable = false)
    @Builder.Default
    private Boolean profileComplete = false;

    /**
     * Indica si el estudiante completó el onboarding inicial.
     * El onboarding es un flujo paso a paso al primer acceso.
     */
    @Column(name = "onboarding_complete", nullable = false)
    @Builder.Default
    private Boolean onboardingComplete = false;

    /** Fecha en que la escuela envió la invitación. */
    @Column(name = "invited_at")
    private LocalDateTime invitedAt;

    /** Fecha en que el estudiante aceptó la invitación y se registró. */
    @Column(name = "registered_at")
    private LocalDateTime registeredAt;

    // ──────────────────────────────────────
    // Relaciones con documents, keywords y países
    // ──────────────────────────────────────

    /**
     * Documentos subidos por el estudiante (CV, carta, foto).
     * Al menos el CV es obligatorio para perfil completo.
     */
    @OneToMany(mappedBy = "student", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    @Builder.Default
    private List<StudentDocument> documents = new ArrayList<>();

    /**
     * Keywords de habilidades/áreas del estudiante.
     * Usadas en el algoritmo de matching (70% del peso).
     */
    @ManyToMany(fetch = FetchType.LAZY)
    @JoinTable(
        name = "student_keywords",
        joinColumns = @JoinColumn(name = "student_id"),
        inverseJoinColumns = @JoinColumn(name = "keyword_id")
    )
    @Builder.Default
    private List<Keyword> keywords = new ArrayList<>();

    /**
     * Países de preferencia del estudiante (máximo 5).
     * Usados en el matching (20% del peso).
     */
    @ElementCollection
    @CollectionTable(name = "student_country_preferences",
        joinColumns = @JoinColumn(name = "student_id"))
    @Column(name = "country_code")
    @Builder.Default
    private List<String> countryPreferences = new ArrayList<>();

    /**
     * Solicitudes enviadas por el estudiante.
     */
    @OneToMany(mappedBy = "student", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    @Builder.Default
    private List<Application> applications = new ArrayList<>();

    @CreationTimestamp
    @Column(name = "created_at", updatable = false)
    private LocalDateTime createdAt;

    @UpdateTimestamp
    @Column(name = "updated_at")
    private LocalDateTime updatedAt;
}
