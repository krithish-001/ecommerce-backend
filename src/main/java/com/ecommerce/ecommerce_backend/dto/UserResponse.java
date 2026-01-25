package com.ecommerce.ecommerce_backend.dto;

import com.ecommerce.ecommerce_backend.model.User;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * DTO for user response
 * Sent back to client after login/registration
 *
 * WHY?
 * - Never send password back to client
 * - Only send necessary user info (id, email, name, role)
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class UserResponse {

    private Long id;
    private String email;
    private String firstName;
    private String lastName;
    private String phone;
    private String role;
    private Boolean isActive;

    /**
     * Convert entity to DTO
     */
    public static UserResponse fromEntity(User user) {
        return UserResponse.builder()
                .id(user.getId())
                .email(user.getEmail())
                .firstName(user.getFirstName())
                .lastName(user.getLastName())
                .phone(user.getPhone())
                .role(user.getRole().toString())
                .isActive(user.getIsActive())
                .build();
    }
}