package com.ecommerce.ecommerce_backend.repository;

import com.ecommerce.ecommerce_backend.model.Order;
import com.ecommerce.ecommerce_backend.model.Order.OrderStatus;
import com.ecommerce.ecommerce_backend.model.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import java.util.List;
import java.util.Optional;

/**
 * Repository for Order entity
 * Handles order queries
 */
@Repository
public interface OrderRepository extends JpaRepository<Order, Long> {

    /**
     * Get all orders of a specific user by User object
     */
    List<Order> findByUser(User user);

    /**
     * Get orders by status
     * Admin might want to see PENDING orders
     */
    List<Order> findByStatus(OrderStatus status);

    /**
     * Find order by ID and user (security check)
     * Ensures user can only access their own orders
     */
    Optional<Order> findByIdAndUser(Long id, User user);

    /**
     * Custom query to get all orders
     */
    @Query("SELECT o FROM Order o")
    List<Order> getAllOrders();
}