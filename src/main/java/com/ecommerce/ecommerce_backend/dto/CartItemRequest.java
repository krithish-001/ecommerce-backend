package com.ecommerce.ecommerce_backend.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * DTO for adding item to cart
 * Client sends product ID and quantity
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class CartItemRequest {

    private Long productId;
    private Integer quantity;
}