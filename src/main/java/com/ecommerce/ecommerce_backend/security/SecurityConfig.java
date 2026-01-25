package com.ecommerce.ecommerce_backend.security;

import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.security.config.annotation.method.configuration.EnableGlobalMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;

/**
 * Security Configuration
 *
 * WHY?
 * - Configures Spring Security for our app
 * - Defines password encoding
 * - Sets up JWT filter
 * - Configures which endpoints are public/protected
 * - Enables @PreAuthorize for role-based access
 */
@Configuration
@EnableWebSecurity
@EnableGlobalMethodSecurity(prePostEnabled = true) // Enables @PreAuthorize
@RequiredArgsConstructor
public class SecurityConfig {

    private final JwtAuthenticationFilter jwtAuthenticationFilter;

    /**
     * Create PasswordEncoder bean
     * BCrypt: secure password hashing with salt
     */
    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder(12);
    }

    /**
     * Configure HTTP security
     * Define which endpoints require authentication
     * Add JWT filter to the security filter chain
     */
    @Bean
    public SecurityFilterChain filterChain(HttpSecurity http) throws Exception {
        http
                // Disable CSRF (not needed for stateless JWT API)
                .csrf(csrf -> csrf.disable())

                // Stateless session (JWT doesn't use sessions)
                .sessionManagement(session -> session
                        .sessionCreationPolicy(SessionCreationPolicy.STATELESS)
                )

                // Configure endpoint access
                .authorizeHttpRequests(auth -> auth
                        // PUBLIC endpoints (no authentication required)
                        .requestMatchers("/api/auth/**").permitAll()
                        .requestMatchers(HttpMethod.GET, "/api/products").permitAll()
                        .requestMatchers(HttpMethod.GET, "/api/products/**").permitAll()

                        // PRODUCT endpoints (authenticated users - for testing)
                        .requestMatchers(HttpMethod.POST, "/api/products").authenticated()
                        .requestMatchers(HttpMethod.PUT, "/api/products/**").authenticated()
                        .requestMatchers(HttpMethod.DELETE, "/api/products/**").authenticated()

                        // All other endpoints require authentication
                        .anyRequest().authenticated()
                )

                // Use HTTP Basic authentication (for Postman testing without JWT)
                .httpBasic(basic -> {})

                // Add JWT filter before UsernamePasswordAuthenticationFilter
                .addFilterBefore(jwtAuthenticationFilter, UsernamePasswordAuthenticationFilter.class);

        return http.build();
    }
}