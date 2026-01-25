package com.ecommerce.ecommerce_backend.repository;

import com.ecommerce.ecommerce_backend.model.OrderItem;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import java.util.List;

/**
 * Repository for OrderItem entity
 * Individual items in an order
 */
@Repository
public interface OrderItemRepository extends JpaRepository<OrderItem, Long> {

    /**
     * Get all items in an order
     * Used when displaying order details
     */
    List<OrderItem> findByOrderId(Long orderId);
}