package com.ecommerce.ecommerce_backend.security;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.util.Collections;

/**
 * JWT Authentication Filter
 * Runs on every request to validate JWT token
 *
 * WHY a filter?
 * - Executes before controller methods
 * - Validates token on every request
 * - Sets user in SecurityContext if token is valid
 * - Allows @PreAuthorize to work
 *
 * Flow:
 * 1. Extract token from "Authorization: Bearer <token>" header
 * 2. Validate token signature and expiration
 * 3. Extract user info from token
 * 4. Set user in SecurityContext
 * 5. Continue to next filter/controller
 */
@Component
@RequiredArgsConstructor
@Slf4j
public class JwtAuthenticationFilter extends OncePerRequestFilter {

    private final JwtTokenProvider jwtTokenProvider;

    /**
     * Filter method - runs on every HTTP request
     */
    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain filterChain)
            throws ServletException, IOException {

        try {
            // Get Authorization header
            String authHeader = request.getHeader("Authorization");

            if (authHeader == null || authHeader.isEmpty()) {
                log.debug("No Authorization header found");
                filterChain.doFilter(request, response);
                return;
            }

            // Extract token from "Bearer <token>" format
            String token = jwtTokenProvider.extractTokenFromHeader(authHeader);

            if (token == null) {
                log.warn("Invalid Authorization header format");
                filterChain.doFilter(request, response);
                return;
            }

            // Validate token
            if (!jwtTokenProvider.validateToken(token)) {
                log.warn("Invalid or expired JWT token");
                filterChain.doFilter(request, response);
                return;
            }

            // Extract user info from token
            Long userId = jwtTokenProvider.getUserIdFromToken(token);
            String email = jwtTokenProvider.getEmailFromToken(token);
            String role = jwtTokenProvider.getRoleFromToken(token);

            if (userId == null || email == null || role == null) {
                log.warn("Failed to extract user info from token");
                filterChain.doFilter(request, response);
                return;
            }

            log.info("JWT token validated for user: {}", email);

            // Create authentication token
            // Principal: email (username)
            // Credentials: token
            // Authorities: user's role
            UsernamePasswordAuthenticationToken authentication =
                    new UsernamePasswordAuthenticationToken(
                            email, // principal (user identifier)
                            token, // credentials
                            Collections.singleton(new SimpleGrantedAuthority("ROLE_" + role)) // authorities
                    );

            // Set custom attribute so controller can access user ID
            authentication.setDetails(userId);

            // Set authentication in SecurityContext
            // This allows @PreAuthorize("hasRole('ADMIN')") to work
            SecurityContextHolder.getContext().setAuthentication(authentication);

            log.debug("Security context updated for user: {}", email);

        } catch (Exception e) {
            log.error("Error processing JWT token", e);
        }

        // Continue to next filter
        filterChain.doFilter(request, response);
    }
}