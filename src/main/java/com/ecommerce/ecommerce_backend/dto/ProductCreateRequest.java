package com.ecommerce.ecommerce_backend.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * DTO for creating/updating products
 * Only admin can use this
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class ProductCreateRequest {

    private String name;
    private String description;
    private Double price;
    private Integer quantity;
    private String category;
    private String imageUrl;
}