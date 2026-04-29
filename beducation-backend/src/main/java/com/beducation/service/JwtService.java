package com.beducation.service;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import javax.crypto.SecretKey;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import java.util.function.Function;

/**
 * SERVICIO: JwtService
 * --------------------
 * Gestiona la seguridad basada en tokens para el flujo local (no Auth0).
 * Esto es vital para el acceso de estudiantes invitados y funcionalidades 
 * de depuración que requieren bypass de proveedores externos.
 */
@Service
public class JwtService {

    // Clave secreta para firmar los tokens. Se externaliza en application.yml
    @Value("${app.jwt.secret:base64-generated-secret-key-must-be-long-enough-32-chars-at-least}")
    private String secret;

    // Tiempo de vida del token (por defecto 24 horas)
    @Value("${app.jwt.expiration:86400000}") 
    private long jwtExpiration;

    /**
     * Recupera la clave de firma asegurando que tenga la longitud mínima.
     */
    private SecretKey getSigningKey() {
        byte[] keyBytes = secret.getBytes();
        if (keyBytes.length < 32) {
            // Fallback para evitar errores en arranque local si la clave es corta
            return Keys.hmacShaKeyFor("a-very-long-and-secure-fallback-secret-key-for-development-32-chars".getBytes());
        }
        return Keys.hmacShaKeyFor(keyBytes);
    }

    /**
     * Extrae el email (subject) del token.
     */
    public String extractUsername(String token) {
        return extractClaim(token, Claims::getSubject);
    }

    /**
     * Método genérico para extraer cualquier información (Claim) del token.
     */
    public <T> T extractClaim(String token, Function<Claims, T> claimsResolver) {
        final Claims claims = extractAllClaims(token);
        return claimsResolver.apply(claims);
    }

    /**
     * Genera un token nuevo incluyendo el rol y claims personalizados de BeDucation.
     */
    public String generateToken(String email, String role) {
        Map<String, Object> extraClaims = new HashMap<>();
        extraClaims.put("role", role);
        // Mantenemos compatibilidad con el esquema de claims de Auth0 usado en el frontend
        extraClaims.put("https://beducation.com/role", role);
        return generateToken(extraClaims, email);
    }

    /**
     * Construcción y firma del JWT.
     */
    public String generateToken(Map<String, Object> extraClaims, String subject) {
        return Jwts.builder()
                .claims(extraClaims)
                .subject(subject)
                .issuedAt(new Date(System.currentTimeMillis()))
                .expiration(new Date(System.currentTimeMillis() + jwtExpiration))
                .signWith(getSigningKey())
                .compact();
    }

    /**
     * Valida si el token pertenece al usuario y no ha expirado.
     */
    public boolean isTokenValid(String token, String userEmail) {
        final String username = extractUsername(token);
        return (username.equals(userEmail)) && !isTokenExpired(token);
    }

    private boolean isTokenExpired(String token) {
        return extractExpiration(token).before(new Date());
    }

    private Date extractExpiration(String token) {
        return extractClaim(token, Claims::getExpiration);
    }

    /**
     * Parsea el token y recupera el payload (Claims).
     */
    private Claims extractAllClaims(String token) {
        return Jwts.parser()
                .verifyWith(getSigningKey())
                .build()
                .parseSignedClaims(token)
                .getPayload();
    }
}

