package com.beducation.config;

import org.springframework.security.oauth2.core.OAuth2Error;
import org.springframework.security.oauth2.core.OAuth2TokenValidator;
import org.springframework.security.oauth2.core.OAuth2TokenValidatorResult;
import org.springframework.security.oauth2.jwt.Jwt;

/**
 * ============================================================
 * Validador: AudienceValidator
 * ============================================================
 * Valida que el token JWT de Auth0 esté dirigido a nuestra API.
 *
 * La "audiencia" (aud) del JWT identifica a qué servicio va
 * destinado el token. Si no coincide con nuestro audience
 * configurado, el token se rechaza con 401 Unauthorized.
 *
 * Esto previene que tokens de otras aplicaciones Auth0 puedan
 * usarse para acceder a nuestra API.
 * ============================================================
 */
public class AudienceValidator implements OAuth2TokenValidator<Jwt> {

    /** Audiencia esperada (configurada en application.yml como app.auth0.audience) */
    private final String audience;

    public AudienceValidator(String audience) {
        this.audience = audience;
    }

    /**
     * Valida que el JWT contenga la audiencia correcta.
     * El claim "aud" del JWT puede ser un String o una lista de Strings.
     *
     * @param jwt el token JWT a validar
     * @return SUCCESS si la audiencia es correcta, FAILURE en caso contrario
     */
    @Override
    public OAuth2TokenValidatorResult validate(Jwt jwt) {
        if (jwt.getAudience().contains(audience)) {
            return OAuth2TokenValidatorResult.success();
        }

        // Token rechazado: la audiencia no coincide
        OAuth2Error error = new OAuth2Error(
            "invalid_token",
            "El token no contiene la audiencia requerida: " + audience,
            null
        );
        return OAuth2TokenValidatorResult.failure(error);
    }
}
