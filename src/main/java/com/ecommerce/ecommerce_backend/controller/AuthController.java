package com.ecommerce.ecommerce_backend.controller;

import com.ecommerce.ecommerce_backend.dto.ApiResponse;
import com.ecommerce.ecommerce_backend.dto.AuthResponse;
import com.ecommerce.ecommerce_backend.dto.UserLoginRequest;
import com.ecommerce.ecommerce_backend.dto.UserRegisterRequest;
import com.ecommerce.ecommerce_backend.dto.UserResponse;
import com.ecommerce.ecommerce_backend.model.User;
import com.ecommerce.ecommerce_backend.security.JwtTokenProvider;
import com.ecommerce.ecommerce_backend.service.UserService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * Authentication Controller
 * Handles user registration and login
 *
 * WHY @RestController?
 * - Combines @Controller + @ResponseBody
 * - Automatically converts responses to JSON
 * - No template rendering needed
 *
 * WHY @RequestMapping("/api/auth")?
 * - All endpoints start with /api/auth
 * - e.g., POST /api/auth/register
 */
@RestController
@RequestMapping("/api/auth")
@RequiredArgsConstructor
@Slf4j
public class AuthController {

    private final UserService userService;
    private final JwtTokenProvider jwtTokenProvider;

    /**
     * Register new user
     *
     * Endpoint: POST /api/auth/register
     * Body: {
     *   "email": "user@example.com",
     *   "password": "password123",
     *   "confirmPassword": "password123",
     *   "firstName": "John",
     *   "lastName": "Doe",
     *   "phone": "1234567890"
     * }
     *
     * Response: 201 Created with user data
     */
    @PostMapping("/register")
    public ResponseEntity<ApiResponse<UserResponse>> register(@RequestBody UserRegisterRequest request) {
        log.info("Register request for email: {}", request.getEmail());

        try {
            // Call service to register
            UserResponse userResponse = userService.register(request);

            // Return success response
            ApiResponse<UserResponse> response = ApiResponse.success(
                    "User registered successfully",
                    userResponse
            );

            return ResponseEntity.status(HttpStatus.CREATED).body(response);

        } catch (IllegalArgumentException e) {
            log.error("Registration failed: {}", e.getMessage());

            ApiResponse<UserResponse> errorResponse = ApiResponse.error(
                    "Registration failed",
                    e.getMessage()
            );

            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(errorResponse);

        } catch (Exception e) {
            log.error("Unexpected error during registration", e);

            ApiResponse<UserResponse> errorResponse = ApiResponse.error(
                    "Registration failed",
                    "An unexpected error occurred"
            );

            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(errorResponse);
        }
    }

    /**
     * Login user
     *
     * Endpoint: POST /api/auth/login
     * Body: {
     *   "email": "user@example.com",
     *   "password": "password123"
     * }
     *
     * Response: 200 OK with JWT token
     *
     * NOTE: JWT implementation comes in next security step
     * For now, we'll return success message
     */
    @PostMapping("/login")
    public ResponseEntity<ApiResponse<AuthResponse>> login(@RequestBody UserLoginRequest request) {
        log.info("Login request for email: {}", request.getEmail());

        try {
            // Get user from database
            User user = userService.getUserByEmail(request.getEmail());

            // Verify password
            if (!userService.verifyPassword(request.getPassword(), user.getPassword())) {
                throw new IllegalArgumentException("Invalid email or password");
            }

            // Check if user is active
            if (!user.getIsActive()) {
                throw new IllegalArgumentException("User account is inactive");
            }

            // Convert to DTO
            UserResponse userResponse = UserResponse.fromEntity(user);

            // Generate JWT token
            String token = jwtTokenProvider.generateToken(
                    user.getId(),
                    user.getEmail(),
                    user.getRole().toString()
            );

            // Create auth response
            AuthResponse authResponse = AuthResponse.builder()
                    .token(token)
                    .user(userResponse)
                    .message("Login successful")
                    .build();

            ApiResponse<AuthResponse> response = ApiResponse.success(
                    "Login successful",
                    authResponse
            );

            return ResponseEntity.ok(response);

        } catch (IllegalArgumentException e) {
            log.error("Login failed: {}", e.getMessage());

            ApiResponse<AuthResponse> errorResponse = ApiResponse.error(
                    "Login failed",
                    e.getMessage()
            );

            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(errorResponse);

        } catch (Exception e) {
            log.error("Unexpected error during login", e);

            ApiResponse<AuthResponse> errorResponse = ApiResponse.error(
                    "Login failed",
                    "An unexpected error occurred"
            );

            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(errorResponse);
        }
    }
}