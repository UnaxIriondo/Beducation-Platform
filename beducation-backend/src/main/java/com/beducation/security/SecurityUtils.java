package com.beducation.security;

import com.beducation.model.User;
import com.beducation.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.stereotype.Component;

/**
 * ============================================================
 * Componente: SecurityUtils
 * ============================================================
 * Utilidades para extraer información del usuario autenticado
 * a partir del token JWT de Auth0 en el contexto de seguridad.
 *
 * Auth0 incluye el "sub" (subject) de la forma:
 *   "auth0|60d21b4667701d2bd0fe456f"
 *
 * Este componente actúa como puente entre el JWT y la entidad
 * User de nuestra base de datos.
 * ============================================================
 */
@Component
@RequiredArgsConstructor
@Slf4j
public class SecurityUtils {

    private final UserRepository userRepository;

    /**
     * Obtiene el usuario autenticado actual desde la base de datos.
     * Usa el "sub" del JWT para buscar en la tabla users.
     *
     * @return el User autenticado
     * @throws RuntimeException si no hay usuario autenticado o no existe en BD
     */
    public User getCurrentUser() {
        String auth0Id = getCurrentAuth0Id();
        return userRepository.findByAuth0Id(auth0Id)
            .orElseThrow(() -> new RuntimeException("Usuario no encontrado para auth0Id: " + auth0Id));
    }

    /**
     * Extrae el "sub" (identificador de Auth0) del JWT actual.
     * Lanza excepción si no hay autenticación activa.
     *
     * @return el auth0Id del usuario autenticado
     */
    public String getCurrentAuth0Id() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication == null || !(authentication.getPrincipal() instanceof Jwt jwt)) {
            throw new RuntimeException("No hay usuario autenticado en el contexto de seguridad");
        }
        return jwt.getSubject(); // El "sub" del JWT es el auth0Id
    }

    /**
     * Extrae el email del usuario autenticado desde el JWT.
     * Auth0 incluye el email en el claim "https://beducation.com/email"
     * o directamente como "email" dependiendo de la configuración.
     *
     * @return email del usuario autenticado
     */
    public String getCurrentUserEmail() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication != null && authentication.getPrincipal() instanceof Jwt jwt) {
            // Intentar con claim personalizado primero, luego con el estándar
            String email = jwt.getClaimAsString("https://beducation.com/email");
            if (email == null) {
                email = jwt.getClaimAsString("email");
            }
            return email;
        }
        return null;
    }

    /**
     * Obtiene el rol del usuario autenticado desde el JWT.
     * Auth0 incluye roles customizados en el claim
     * "https://beducation.com/role" (configurado en Auth0 Actions).
     *
     * @return rol como String (SCHOOL, STUDENT, COMPANY, ADMIN)
     */
    public String getCurrentUserRole() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication != null && authentication.getPrincipal() instanceof Jwt jwt) {
            return jwt.getClaimAsString("https://beducation.com/role");
        }
        return null;
    }

    /**
     * Verifica si el usuario autenticado tiene un rol específico.
     * @param role rol a verificar (ej. "ADMIN", "SCHOOL")
     * @return true si el usuario tiene ese rol
     */
    public boolean hasRole(String role) {
        return role.equals(getCurrentUserRole());
    }
}
