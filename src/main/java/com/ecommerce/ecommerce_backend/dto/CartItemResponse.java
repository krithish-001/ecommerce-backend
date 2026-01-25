package com.ecommerce.ecommerce_backend.dto;

import com.ecommerce.ecommerce_backend.model.CartItem;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * DTO for cart item response
 * Shows what's in user's cart
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class CartItemResponse {

    private Long id;
    private Long productId;
    private String productName;
    private Integer quantity;
    private Double price;
    private Double totalPrice;

    /**
     * Convert entity to DTO
     */
    public static CartItemResponse fromEntity(CartItem cartItem) {
        return CartItemResponse.builder()
                .id(cartItem.getId())
                .productId(cartItem.getProduct().getId())
                .productName(cartItem.getProduct().getName())
                .quantity(cartItem.getQuantity())
                .price(cartItem.getPrice())
                .totalPrice(cartItem.getPrice() * cartItem.getQuantity())
                .build();
    }
}