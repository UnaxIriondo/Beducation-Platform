package com.beducation.model;

import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import java.time.LocalDateTime;

/**
 * ============================================================
 * Entidad: User
 * ============================================================
 * Representa a cualquier usuario autenticado de la plataforma.
 * Todos los actores del sistema (Escuela, Estudiante, Empresa,
 * Administrador) tienen un registro en esta tabla.
 *
 * La autenticación se delega a Auth0. El campo auth0Id es el
 * identificador único del proveedor externo ("sub" del JWT).
 *
 * Relaciones:
 *   - Un usuario puede tener UN perfil de School, Student o Company
 *   - Los roles determinan qué portal puede usar cada usuario
 * ============================================================
 */
@Entity
@Table(name = "users", indexes = {
    @Index(name = "idx_user_email", columnList = "email"),
    @Index(name = "idx_user_auth0", columnList = "auth0_id")
})
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class User {

    /**
     * Identificador único interno (auto-incremental en MySQL).
     */
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    /**
     * Identificador único de Auth0 (campo "sub" del JWT).
     * Ejemplo: "auth0|60d21b4667701d2bd0fe456f"
     * Nunca puede ser nulo ni repetirse.
     */
    @Column(name = "auth0_id", unique = true, nullable = false)
    private String auth0Id;

    /**
     * Email del usuario. Extraído del token JWT al primer login.
     * Único en el sistema.
     */
    @Column(nullable = false, unique = true)
    private String email;

    /**
     * Rol del usuario en la plataforma.
     * Determina qué portal y qué endpoints puede usar.
     */
    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private Role role;

    /**
     * Estado de la cuenta del usuario.
     * ACTIVE: puede operar normalmente.
     * SUSPENDED: bloqueado por el administrador.
     */
    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    @Builder.Default
    private UserStatus status = UserStatus.ACTIVE;

    /**
     * Fecha/hora de creación del registro (automática).
     */
    @CreationTimestamp
    @Column(name = "created_at", updatable = false)
    private LocalDateTime createdAt;

    /**
     * Fecha/hora de última actualización (automática).
     */
    @UpdateTimestamp
    @Column(name = "updated_at")
    private LocalDateTime updatedAt;

    // ──────────────────────────────────────
    // Enumeraciones internas
    // ──────────────────────────────────────

    /**
     * Roles disponibles en la plataforma BeDucation.
     * SCHOOL     → Portal de Escuelas
     * STUDENT    → Portal de Estudiantes
     * COMPANY    → Portal de Empresas
     * ADMIN      → Panel de Administración
     */
    public enum Role {
        SCHOOL, STUDENT, COMPANY, ADMIN
    }

    /**
     * Estados posibles de una cuenta de usuario.
     */
    public enum UserStatus {
        ACTIVE, SUSPENDED
    }
}
