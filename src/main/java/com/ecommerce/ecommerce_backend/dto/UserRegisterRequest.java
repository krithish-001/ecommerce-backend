package com.ecommerce.ecommerce_backend.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * DTO for user registration request
 * Client sends this data when registering
 *
 * WHY DTO?
 * - Client only needs to send email, password, firstName, lastName, phone
 * - We don't want client sending id, createdAt, role, etc.
 * - Validation happens here before reaching entity
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class UserRegisterRequest {

    private String email;
    private String password;
    private String confirmPassword;
    private String firstName;
    private String lastName;
    private String phone;

    /**
     * Validate passwords match
     */
    public boolean isPasswordMatching() {
        return password != null && password.equals(confirmPassword);
    }
}