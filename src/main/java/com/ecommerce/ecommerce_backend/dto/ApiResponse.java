package com.ecommerce.ecommerce_backend.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * Generic API response wrapper
 * All API endpoints return this format
 *
 * WHY?
 * - Consistent response structure
 * - Client knows what to expect
 * - Easy to add metadata (timestamp, pagination, etc.)
 *
 * Usage:
 * ApiResponse<ProductResponse> response = ApiResponse.<ProductResponse>builder()
 *     .success(true)
 *     .message("Product created successfully")
 *     .data(productResponse)
 *     .build();
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ApiResponse<T> {

    private boolean success;
    private String message;
    private T data;
    private String error;
    private Long timestamp;

    /**
     * Success response
     */
    public static <T> ApiResponse<T> success(String message, T data) {
        return ApiResponse.<T>builder()
                .success(true)
                .message(message)
                .data(data)
                .timestamp(System.currentTimeMillis())
                .build();
    }

    /**
     * Error response
     */
    public static <T> ApiResponse<T> error(String message, String error) {
        return ApiResponse.<T>builder()
                .success(false)
                .message(message)
                .error(error)
                .timestamp(System.currentTimeMillis())
                .build();
    }

    /**
     * Success response without data
     */
    public static <T> ApiResponse<T> success(String message) {
        return ApiResponse.<T>builder()
                .success(true)
                .message(message)
                .timestamp(System.currentTimeMillis())
                .build();
    }
}