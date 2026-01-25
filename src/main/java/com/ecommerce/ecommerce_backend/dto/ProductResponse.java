package com.ecommerce.ecommerce_backend.dto;

import com.ecommerce.ecommerce_backend.model.Product;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * DTO for product response
 * Sent to clients when they request products
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ProductResponse {

    private Long id;
    private String name;
    private String description;
    private Double price;
    private Integer quantity;
    private String category;
    private String imageUrl;
    private Boolean isActive;

    /**
     * Convert entity to DTO
     */
    public static ProductResponse fromEntity(Product product) {
        return ProductResponse.builder()
                .id(product.getId())
                .name(product.getName())
                .description(product.getDescription())
                .price(product.getPrice())
                .quantity(product.getQuantity())
                .category(product.getCategory())
                .imageUrl(product.getImageUrl())
                .isActive(product.getIsActive())
                .build();
    }
}