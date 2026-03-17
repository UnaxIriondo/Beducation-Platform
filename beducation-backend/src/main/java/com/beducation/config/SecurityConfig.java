package com.beducation.config;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.oauth2.core.DelegatingOAuth2TokenValidator;
import org.springframework.security.oauth2.core.OAuth2TokenValidator;
import org.springframework.security.oauth2.jwt.*;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.CorsConfigurationSource;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;

import java.util.Arrays;
import java.util.List;

/**
 * ============================================================
 * Configuración: SecurityConfig
 * ============================================================
 * Configura Spring Security para la API REST de BeDucation.
 *
 * ESTRATEGIA DE SEGURIDAD:
 * ─────────────────────────────────────────────────────────────
 * 1. Stateless: no hay sesiones HTTP (JWT en cada petición)
 * 2. OAuth2 Resource Server: valida tokens JWT de Auth0
 * 3. CORS: permite peticiones desde el frontend Vue.js
 * 4. Autorización por roles mediante @PreAuthorize en los métodos
 *
 * ENDPOINTS PÚBLICOS (sin autenticación):
 *   - GET /actuator/health → monitoreo
 *   - POST /schools        → registro de escuelas
 *   - POST /companies      → registro de empresas
 *   - GET /docs            → Swagger UI (desarrollo)
 *
 * TODOS LOS DEMÁS ENDPOINTS requieren JWT válido de Auth0.
 * ─────────────────────────────────────────────────────────────
 */
@Configuration
@EnableWebSecurity
@EnableMethodSecurity(prePostEnabled = true) // Activa @PreAuthorize en los servicios
public class SecurityConfig {

    /** URL del emisor (issuer) de Auth0 — del application.yml */
    @Value("${spring.security.oauth2.resourceserver.jwt.issuer-uri:https://dev-placeholder.auth0.com/}")
    private String issuerUri;
    /** Audiencia esperada en el JWT — identifica nuestra API */
    @Value("${app.auth0.audience}")
    private String audience;

    /** URL del frontend Vue.js para configurar CORS */
    @Value("${app.frontend.url}")
    private String frontendUrl;

    /**
     * Cadena principal de filtros de seguridad HTTP.
     * Define qué endpoints son públicos y cuáles requieren autenticación.
     *
     * @param http objeto HttpSecurity de Spring
     * @return la cadena de filtros configurada
     */
    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
        http
            // ── Sin estado: no guardar sesiones en el servidor
            .sessionManagement(sm ->
                sm.sessionCreationPolicy(SessionCreationPolicy.STATELESS))

            // ── Configuración CORS (permitir peticiones del frontend)
            .cors(cors -> cors.configurationSource(corsConfigurationSource()))

            // ── Desactivar CSRF (innecesario en APIs REST con JWT)
            .csrf(csrf -> csrf.disable())

            // ── Reglas de autorización por endpoint
            .authorizeHttpRequests(auth -> auth
                // Endpoints completamente públicos
                .requestMatchers(HttpMethod.GET,
                    "/actuator/health",
                    "/v3/api-docs/**",
                    "/swagger-ui/**",
                    "/swagger-ui.html"
                ).permitAll()

                // Registro de escuelas y empresas (antes de autenticarse)
                .requestMatchers(HttpMethod.POST, "/schools", "/companies").permitAll()

                // Todos los demás endpoints requieren JWT válido
                .anyRequest().authenticated()
            )

            // ── Configurar como Resource Server OAuth2 (valida JWT)
            .oauth2ResourceServer(oauth2 ->
                oauth2.jwt(jwt -> jwt.decoder(jwtDecoder()))
            );

        return http.build();
    }

    /**
     * Decodificador y validador de tokens JWT de Auth0.
     *
     * Valida:
     *   1. Firma del token (usando las claves públicas de Auth0 JWKS)
     *   2. Que el emisor (issuer) sea nuestro tenant de Auth0
     *   3. Que la audiencia (audience) corresponda a nuestra API
     *   4. Que el token no haya expirado (automático)
     *
     * @return JwtDecoder configurado con validadores de Auth0
     */@Bean
public JwtDecoder jwtDecoder() {
    // Si es el placeholder de desarrollo, devuelve un decoder que no valida
    if (issuerUri.contains("dev-placeholder")) {
        return token -> {
            throw new org.springframework.security.oauth2.jwt.BadJwtException("Auth0 no configurado - modo desarrollo");
        };
    }
    NimbusJwtDecoder jwtDecoder = JwtDecoders.fromOidcIssuerLocation(issuerUri);
    OAuth2TokenValidator<Jwt> audienceValidator = new AudienceValidator(audience);
    OAuth2TokenValidator<Jwt> issuerValidator = JwtValidators.createDefaultWithIssuer(issuerUri);
    OAuth2TokenValidator<Jwt> combinedValidator = new DelegatingOAuth2TokenValidator<>(issuerValidator, audienceValidator);
    jwtDecoder.setJwtValidator(combinedValidator);
    return jwtDecoder;
}

    /**
     * Configuración de CORS (Cross-Origin Resource Sharing).
     *
     * Permite al frontend Vue.js (localhost:5173 en desarrollo) hacer
     * peticiones a la API. En producción, se reemplaza por el dominio real.
     *
     * @return fuente de configuración CORS
     */
    @Bean
    public CorsConfigurationSource corsConfigurationSource() {
        CorsConfiguration configuration = new CorsConfiguration();

        // Orígenes permitidos: el frontend Vue.js
        configuration.setAllowedOrigins(List.of(frontendUrl));

        // Métodos HTTP permitidos para todas las operaciones CRUD
        configuration.setAllowedMethods(Arrays.asList(
            "GET", "POST", "PUT", "PATCH", "DELETE", "OPTIONS"
        ));

        // Cabeceras permitidas (incluir Authorization para el JWT)
        configuration.setAllowedHeaders(Arrays.asList(
            "Authorization", "Content-Type", "Accept", "X-Requested-With"
        ));

        // Permitir cookies/credenciales si fuera necesario
        configuration.setAllowCredentials(true);

        // Tiempo en caché del resultado del preflight (OPTIONS)
        configuration.setMaxAge(3600L);

        // Aplicar a todas las rutas de la API
        UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
        source.registerCorsConfiguration("/**", configuration);
        return source;
    }
}
