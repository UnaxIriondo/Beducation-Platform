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
 * ============================================================
 * Servicio: JwtService
 * ============================================================
 * Gestiona la generación y validación de tokens JWT nativos
 * para el inicio de sesión con email y contraseña (estudiantes).
 * ============================================================
 */
@Service
public class JwtService {

    @Value("${app.jwt.secret:base64-generated-secret-key-must-be-long-enough-32-chars-at-least}")
    private String secret;

    @Value("${app.jwt.expiration:86400000}") // 24 hours
    private long jwtExpiration;

    private SecretKey getSigningKey() {
        byte[] keyBytes = secret.getBytes();
        if (keyBytes.length < 32) {
            // Fallback for development if the key is too short
            return Keys.hmacShaKeyFor("a-very-long-and-secure-fallback-secret-key-for-development-32-chars".getBytes());
        }
        return Keys.hmacShaKeyFor(keyBytes);
    }

    public String extractUsername(String token) {
        return extractClaim(token, Claims::getSubject);
    }

    public <T> T extractClaim(String token, Function<Claims, T> claimsResolver) {
        final Claims claims = extractAllClaims(token);
        return claimsResolver.apply(claims);
    }

    public String generateToken(String email, String role) {
        Map<String, Object> extraClaims = new HashMap<>();
        extraClaims.put("role", role);
        extraClaims.put("https://beducation.com/role", role);
        return generateToken(extraClaims, email);
    }

    public String generateToken(Map<String, Object> extraClaims, String subject) {
        return Jwts.builder()
                .claims(extraClaims)
                .subject(subject)
                .issuedAt(new Date(System.currentTimeMillis()))
                .expiration(new Date(System.currentTimeMillis() + jwtExpiration))
                .signWith(getSigningKey())
                .compact();
    }

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

    private Claims extractAllClaims(String token) {
        return Jwts.parser()
                .verifyWith(getSigningKey())
                .build()
                .parseSignedClaims(token)
                .getPayload();
    }
}
