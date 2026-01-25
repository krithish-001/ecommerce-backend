package com.ecommerce.ecommerce_backend.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * DTO for login request
 * Client sends only email and password
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class UserLoginRequest {

    private String email;
    private String password;
}