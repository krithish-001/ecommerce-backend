package com.ecommerce.ecommerce_backend.dto;

import com.ecommerce.ecommerce_backend.model.Cart;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import java.util.List;
import java.util.stream.Collectors;

/**
 * DTO for cart response
 * Shows user's complete cart with all items
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class CartResponse {

    private Long id;
    private Long userId;
    private List<CartItemResponse> items;
    private Double totalPrice;

    /**
     * Convert entity to DTO
     */
    public static CartResponse fromEntity(Cart cart) {
        List<CartItemResponse> items = cart.getCartItems() != null
                ? cart.getCartItems().stream()
                .map(CartItemResponse::fromEntity)
                .collect(Collectors.toList())
                : List.of();

        Double totalPrice = items.stream()
                .mapToDouble(item -> item.getTotalPrice())
                .sum();

        return CartResponse.builder()
                .id(cart.getId())
                .userId(cart.getUser().getId())
                .items(items)
                .totalPrice(totalPrice)
                .build();
    }
}