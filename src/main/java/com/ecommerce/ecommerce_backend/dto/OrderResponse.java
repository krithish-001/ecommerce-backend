package com.ecommerce.ecommerce_backend.dto;

import com.ecommerce.ecommerce_backend.model.Order;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import java.util.List;
import java.util.stream.Collectors;

/**
 * DTO for order response
 * Shows complete order details with all items
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class OrderResponse {

    private Long id;
    private Long userId;
    private List<OrderItemResponse> items;
    private Double totalPrice;
    private String status;
    private String shippingAddress;
    private String phoneNumber;
    private Long createdAt;

    /**
     * Convert entity to DTO
     */
    public static OrderResponse fromEntity(Order order) {
        List<OrderItemResponse> items = order.getOrderItems() != null
                ? order.getOrderItems().stream()
                .map(OrderItemResponse::fromEntity)
                .collect(Collectors.toList())
                : List.of();

        return OrderResponse.builder()
                .id(order.getId())
                .userId(order.getUser().getId())
                .items(items)
                .totalPrice(order.getTotalPrice())
                .status(order.getStatus().toString())
                .shippingAddress(order.getShippingAddress())
                .phoneNumber(order.getPhoneNumber())
                .createdAt(order.getCreatedAt())
                .build();
    }
}