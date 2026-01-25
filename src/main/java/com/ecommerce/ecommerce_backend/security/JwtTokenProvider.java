package com.ecommerce.ecommerce_backend.security;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.SignatureAlgorithm;
import io.jsonwebtoken.security.Keys;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import javax.crypto.SecretKey;
import java.util.Date;

/**
 * JWT Token Provider
 * Handles JWT token generation and validation
 * WHY JWT?
 * - Stateless authentication (no session storage needed)
 * - Secure (digitally signed)
 * - Can store user info in token payload
 * - Works great for REST APIs and mobile apps
 * JWT Structure: Header.Payload.Signature
 * - Header: Algorithm (HS256) and type (JWT)
 * - Payload: Claims (user data)
 * - Signature: Ensures token hasn't been tampered with
 */
@Component
@Slf4j
public class JwtTokenProvider {

    @Value("${jwt.secret}")
    private String jwtSecret;

    @Value("${jwt.expiration}")
    private long jwtExpiration;

    /**
     * Generate JWT token for user
     *
     * @param userId - User's database ID
     * @param email - User's email
     * @param role - User's role (USER or ADMIN)
     * @return JWT token string
     */
    public String generateToken(Long userId, String email, String role) {
        log.info("Generating JWT token for user: {}", email);

        Date now = new Date();
        Date expiryDate = new Date(now.getTime() + jwtExpiration);

        try {
            SecretKey key = Keys.hmacShaKeyFor(jwtSecret.getBytes());

            String token = Jwts.builder()
                    .setSubject(userId.toString())
                    .claim("email", email)
                    .claim("role", role)
                    .setIssuedAt(now)
                    .setExpiration(expiryDate)
                    .signWith(key, SignatureAlgorithm.HS512)
                    .compact();

            log.info("JWT token generated successfully for user: {}", email);
            return token;

        } catch (Exception e) {
            log.error("Error generating JWT token", e);
            throw new RuntimeException("Failed to generate JWT token", e);
        }
    }

    /**
     * Validate JWT token
     *
     * @param token - JWT token to validate
     * @return true if token is valid, false otherwise
     */
    public boolean validateToken(String token) {
        try {
            SecretKey key = Keys.hmacShaKeyFor(jwtSecret.getBytes());

            Jwts.parserBuilder()
                    .setSigningKey(key)
                    .build()
                    .parseClaimsJws(token);

            log.info("JWT token validated successfully");
            return true;

        } catch (io.jsonwebtoken.security.SecurityException e) {
            log.error("Invalid JWT signature: {}", e.getMessage());
        } catch (io.jsonwebtoken.MalformedJwtException e) {
            log.error("Invalid JWT token: {}", e.getMessage());
        } catch (io.jsonwebtoken.ExpiredJwtException e) {
            log.error("JWT token is expired: {}", e.getMessage());
        } catch (io.jsonwebtoken.UnsupportedJwtException e) {
            log.error("Unsupported JWT token: {}", e.getMessage());
        } catch (IllegalArgumentException e) {
            log.error("JWT claims string is empty: {}", e.getMessage());
        } catch (Exception e) {
            log.error("Unexpected error validating JWT token", e);
        }

        return false;
    }

    /**
     * Get user ID from JWT token
     *
     * @param token - JWT token
     * @return User ID from token subject
     */
    public Long getUserIdFromToken(String token) {
        try {
            SecretKey key = Keys.hmacShaKeyFor(jwtSecret.getBytes());

            Claims claims = Jwts.parserBuilder()
                    .setSigningKey(key)
                    .build()
                    .parseClaimsJws(token)
                    .getBody();

            return Long.parseLong(claims.getSubject());

        } catch (Exception e) {
            log.error("Error extracting user ID from token", e);
            return null;
        }
    }

    /**
     * Get email from JWT token
     *
     * @param token - JWT token
     * @return Email from token claims
     */
    public String getEmailFromToken(String token) {
        try {
            SecretKey key = Keys.hmacShaKeyFor(jwtSecret.getBytes());

            Claims claims = Jwts.parserBuilder()
                    .setSigningKey(key)
                    .build()
                    .parseClaimsJws(token)
                    .getBody();

            return (String) claims.get("email");

        } catch (Exception e) {
            log.error("Error extracting email from token", e);
            return null;
        }
    }

    /**
     * Get role from JWT token
     *
     * @param token - JWT token
     * @return Role from token claims
     */
    public String getRoleFromToken(String token) {
        try {
            SecretKey key = Keys.hmacShaKeyFor(jwtSecret.getBytes());

            Claims claims = Jwts.parserBuilder()
                    .setSigningKey(key)
                    .build()
                    .parseClaimsJws(token)
                    .getBody();

            return (String) claims.get("role");

        } catch (Exception e) {
            log.error("Error extracting role from token", e);
            return null;
        }
    }

    /**
     * Extract JWT token from Authorization header
     * Expected format: "Bearer <token>"
     *
     * @param authHeader - Authorization header value
     * @return JWT token or null if invalid format
     */
    public String extractTokenFromHeader(String authHeader) {
        if (authHeader != null && authHeader.startsWith("Bearer ")) {
            return authHeader.substring(7);
        }
        return null;
    }
}