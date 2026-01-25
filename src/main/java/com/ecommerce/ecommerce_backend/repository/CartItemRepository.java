package com.ecommerce.ecommerce_backend.repository;

import com.ecommerce.ecommerce_backend.model.CartItem;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import java.util.Optional;

/**
 * Repository for CartItem entity
 * Individual items in a cart
 */
@Repository
public interface CartItemRepository extends JpaRepository<CartItem, Long> {

    /**
     * Find cart item by cart ID and product ID
     * Used to check if product already in cart
     */
    Optional<CartItem> findByCartIdAndProductId(Long cartId, Long productId);

    /**
     * Delete item when product removed from cart
     */
    void deleteByCartIdAndProductId(Long cartId, Long productId);

    /**
     * Clear all items from a cart
     */
    void deleteByCartId(Long cartId);
}