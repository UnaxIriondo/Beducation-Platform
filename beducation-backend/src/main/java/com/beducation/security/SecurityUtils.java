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
     * Busca primero por auth0Id (si es JWT) y si no, por Email.
     *
     * @return el User autenticado
     * @throws RuntimeException si no hay usuario autenticado o no existe en BD
     */
    public User getCurrentUser() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication == null) {
            throw new RuntimeException("No hay usuario autenticado en el contexto de seguridad");
        }

        Object principal = authentication.getPrincipal();

        if (principal instanceof Jwt jwt) {
            String auth0Id = jwt.getSubject();
            return userRepository.findByAuth0Id(auth0Id)
                .or(() -> userRepository.findByEmail(jwt.getClaimAsString("email")))
                .or(() -> userRepository.findByEmail(jwt.getClaimAsString("https://beducation.com/email")))
                .orElseThrow(() -> new RuntimeException("Usuario no encontrado para auth0Id/Email del JWT: " + auth0Id));
        } else if (principal instanceof String email) {
            return userRepository.findByEmail(email)
                .orElseThrow(() -> new RuntimeException("Usuario no encontrado para el email: " + email));
        }

        throw new RuntimeException("Tipo de principal no soportado: " + principal.getClass().getName());
    }

    /**
     * Extrae el identificado "sub" o el email dependiendo del tipo de login.
     */
    public String getCurrentAuth0Id() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication == null) throw new RuntimeException("No autenticado");
        
        if (authentication.getPrincipal() instanceof Jwt jwt) {
            return jwt.getSubject();
        }
        return null; // Local users don't have auth0Id
    }

    /**
     * Extrae el email del usuario autenticado.
     */
    public String getCurrentUserEmail() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication == null) return null;

        Object principal = authentication.getPrincipal();
        if (principal instanceof Jwt jwt) {
            String email = jwt.getClaimAsString("https://beducation.com/email");
            return (email != null) ? email : jwt.getClaimAsString("email");
        } else if (principal instanceof String email) {
            return email;
        }
        return null;
    }

    /**
     * Obtiene el rol del usuario autenticado.
     */
    public String getCurrentUserRole() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication == null) return null;

        // Intentar desde JWT
        if (authentication.getPrincipal() instanceof Jwt jwt) {
            String role = jwt.getClaimAsString("https://beducation.com/role");
            if (role != null) return role;
        }
        
        // Intentar desde las autoridades de Spring (SCOPE_ROLE)
        return authentication.getAuthorities().stream()
                .map(auth -> auth.getAuthority().replace("SCOPE_", ""))
                .findFirst()
                .orElse(null);
    }

    /**
     * Verifica si el usuario autenticado tiene un rol específico.
     */
    public boolean hasRole(String role) {
        String currentRole = getCurrentUserRole();
        return currentRole != null && currentRole.equals(role);
    }
}
