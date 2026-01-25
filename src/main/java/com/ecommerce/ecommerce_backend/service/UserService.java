package com.ecommerce.ecommerce_backend.service;

import com.ecommerce.ecommerce_backend.dto.UserRegisterRequest;
import com.ecommerce.ecommerce_backend.dto.UserResponse;
import com.ecommerce.ecommerce_backend.model.Cart;
import com.ecommerce.ecommerce_backend.model.User;
import com.ecommerce.ecommerce_backend.repository.CartRepository;
import com.ecommerce.ecommerce_backend.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * Service for user-related operations
 *
 * WHY @Service?
 * - Marks this as a business logic component
 * - Spring manages it automatically
 * - Allows @Transactional for database transactions
 *
 * WHY @RequiredArgsConstructor?
 * - Lombok generates constructor for all final fields
 * - Dependency injection happens automatically
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class UserService {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final CartRepository cartRepository;

    /**
     * Register a new user
     *
     * Flow:
     * 1. Validate passwords match
     * 2. Check if email already exists
     * 3. Hash password (never store plain text!)
     * 4. Create and save user with USER role
     * 5. Return user info (without password)
     */
    @Transactional
    public UserResponse register(UserRegisterRequest request) {
        log.info("Registering new user with email: {}", request.getEmail());

        // Validate passwords match
        if (!request.isPasswordMatching()) {
            throw new IllegalArgumentException("Passwords do not match");
        }

        // Check if email already exists
        if (userRepository.existsByEmail(request.getEmail())) {
            throw new IllegalArgumentException("Email already registered");
        }

        // Create user entity
        User user = User.builder()
                .email(request.getEmail())
                .password(passwordEncoder.encode(request.getPassword())) // Hash password!
                .firstName(request.getFirstName())
                .lastName(request.getLastName())
                .phone(request.getPhone())
                .role(User.Role.USER) // Default role
                .isActive(true)
                .build();

        // Save to database
        User savedUser = userRepository.save(user);
        log.info("User registered successfully with id: {}", savedUser.getId());

        // Create cart for user automatically
        Cart cart = Cart.builder()
                .user(savedUser)
                .build();
        cartRepository.save(cart);
        log.info("Cart created for user: {}", savedUser.getId());

        // Return DTO (without password)
        return UserResponse.fromEntity(savedUser);
    }

    /**
     * Get user by email
     * Used during login
     */
    public User getUserByEmail(String email) {
        return userRepository.findByEmail(email)
                .orElseThrow(() -> new RuntimeException("User not found with email: " + email));
    }

    /**
     * Check if user exists by email
     */
    public boolean userExists(String email) {
        return userRepository.existsByEmail(email);
    }

    /**
     * Get user by ID
     */
    public UserResponse getUserById(Long userId) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new RuntimeException("User not found with id: " + userId));
        return UserResponse.fromEntity(user);
    }

    /**
     * Verify password
     * Compares plain text password with hashed stored password
     */
    public boolean verifyPassword(String plainPassword, String hashedPassword) {
        return passwordEncoder.matches(plainPassword, hashedPassword);
    }

    /**
     * Update user info
     */
    @Transactional
    public UserResponse updateUser(Long userId, UserRegisterRequest request) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new RuntimeException("User not found"));

        user.setFirstName(request.getFirstName());
        user.setLastName(request.getLastName());
        user.setPhone(request.getPhone());

        User updatedUser = userRepository.save(user);
        return UserResponse.fromEntity(updatedUser);
    }
}