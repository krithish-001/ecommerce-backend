package com.ecommerce.ecommerce_backend.repository;

import com.ecommerce.ecommerce_backend.model.Cart;
import com.ecommerce.ecommerce_backend.model.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import java.util.Optional;

/**
 * Repository for Cart entity
 * One cart per user
 */
@Repository
public interface CartRepository extends JpaRepository<Cart, Long> {

    /**
     * Find cart by user
     * Each user has exactly one cart
     */
    Optional<Cart> findByUser(User user);

    /**
     * Find cart by user ID
     * More efficient than passing User object
     */
    Optional<Cart> findByUserId(Long userId);

    /**
     * Check if cart exists for user
     */
    boolean existsByUserId(Long userId);
}