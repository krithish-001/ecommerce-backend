package com.ecommerce.ecommerce_backend.service;

import com.ecommerce.ecommerce_backend.dto.CartItemRequest;
import com.ecommerce.ecommerce_backend.dto.CartItemResponse;
import com.ecommerce.ecommerce_backend.dto.CartResponse;
import com.ecommerce.ecommerce_backend.model.Cart;
import com.ecommerce.ecommerce_backend.model.CartItem;
import com.ecommerce.ecommerce_backend.model.Product;
import com.ecommerce.ecommerce_backend.model.User;
import com.ecommerce.ecommerce_backend.repository.CartItemRepository;
import com.ecommerce.ecommerce_backend.repository.CartRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import java.util.Optional;

/**
 * Service for shopping cart operations
 * Each user has ONE cart *  contains MANY items
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class CartService {

    private final CartRepository cartRepository;
    private final CartItemRepository cartItemRepository;
    private final ProductService productService;

    /**
     * Get or create cart for user
     * Every user should have a cart
     */
    @Transactional
    public Cart getOrCreateCart(User user) {
        log.info("Getting or creating cart for user: {}", user.getId());

        Optional<Cart> existingCart = cartRepository.findByUser(user);
        if (existingCart.isPresent()) {
            return existingCart.get();
        }

        // Create new cart if doesn't exist
        Cart newCart = Cart.builder()
                .user(user)
                .build();

        return cartRepository.save(newCart);
    }

    /**
     * Add item to cart
     * If product already in cart, update quantity
     */
    @Transactional
    public CartItemResponse addItemToCart(User user, CartItemRequest request) {
        log.info("Adding item to cart. User: {}, Product: {}, Quantity: {}",
                user.getId(), request.getProductId(), request.getQuantity());

        // Validate quantity
        if (request.getQuantity() <= 0) {
            throw new IllegalArgumentException("Quantity must be greater than 0");
        }

        // Get or create cart
        Cart cart = getOrCreateCart(user);

        // Get product
        Product product = productService.getProductEntityById(request.getProductId());

        // Check if product already in cart
        Optional<CartItem> existingItem = cartItemRepository.findByCartIdAndProductId(
                cart.getId(), product.getId());

        CartItem cartItem;
        if (existingItem.isPresent()) {
            // Update quantity
            cartItem = existingItem.get();
            cartItem.setQuantity(cartItem.getQuantity() + request.getQuantity());
            log.info("Updated existing cart item. New quantity: {}", cartItem.getQuantity());
        } else {
            // Create new cart item
            cartItem = CartItem.builder()
                    .cart(cart)
                    .product(product)
                    .quantity(request.getQuantity())
                    .price(product.getPrice())
                    .build();
            log.info("Created new cart item");
        }

        CartItem savedItem = cartItemRepository.save(cartItem);
        return CartItemResponse.fromEntity(savedItem);
    }

    /**
     * Get user's cart with all items
     */
    public CartResponse getCart(User user) {
        log.info("Fetching cart for user: {}", user.getId());

        Cart cart = cartRepository.findByUser(user)
                .orElseThrow(() -> new RuntimeException("Cart not found for user"));

        return CartResponse.fromEntity(cart);
    }

    /**
     * Remove item from cart
     */
    @Transactional
    public void removeItemFromCart(User user, Long productId) {
        log.info("Removing item from cart. User: {}, Product: {}", user.getId(), productId);

        Cart cart = cartRepository.findByUser(user)
                .orElseThrow(() -> new RuntimeException("Cart not found"));

        cartItemRepository.deleteByCartIdAndProductId(cart.getId(), productId);
        log.info("Item removed from cart");
    }

    /**
     * Update item quantity
     */
    @Transactional
    public CartItemResponse updateItemQuantity(User user, Long productId, Integer newQuantity) {
        log.info("Updating item quantity. User: {}, Product: {}, New Quantity: {}",
                user.getId(), productId, newQuantity);

        if (newQuantity <= 0) {
            throw new IllegalArgumentException("Quantity must be greater than 0");
        }

        Cart cart = cartRepository.findByUser(user)
                .orElseThrow(() -> new RuntimeException("Cart not found"));

        CartItem cartItem = cartItemRepository.findByCartIdAndProductId(cart.getId(), productId)
                .orElseThrow(() -> new RuntimeException("Item not found in cart"));

        cartItem.setQuantity(newQuantity);
        CartItem updatedItem = cartItemRepository.save(cartItem);

        return CartItemResponse.fromEntity(updatedItem);
    }

    /**
     * Clear entire cart
     * Called after successful order creation
     */
    @Transactional
    public void clearCart(User user) {
        log.info("Clearing cart for user: {}", user.getId());

        Cart cart = cartRepository.findByUser(user)
                .orElseThrow(() -> new RuntimeException("Cart not found"));

        cartItemRepository.deleteByCartId(cart.getId());
        log.info("Cart cleared");
    }

    /**
     * Get cart item count
     */
    public Long getCartItemCount(User user) {
        Cart cart = cartRepository.findByUser(user)
                .orElseThrow(() -> new RuntimeException("Cart not found"));

        return (long) (cart.getCartItems() != null ? cart.getCartItems().size() : 0);
    }
}