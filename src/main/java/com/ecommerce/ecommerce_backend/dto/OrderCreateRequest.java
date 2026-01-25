package com.ecommerce.ecommerce_backend.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * DTO for creating an order
 * Client sends shipping details
 * We use their cart to create the order
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class OrderCreateRequest {

    private String shippingAddress;
    private String phoneNumber;
}