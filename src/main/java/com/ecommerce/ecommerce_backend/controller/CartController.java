package com.ecommerce.ecommerce_backend.controller;

import com.ecommerce.ecommerce_backend.dto.ApiResponse;
import com.ecommerce.ecommerce_backend.dto.CartItemRequest;
import com.ecommerce.ecommerce_backend.dto.CartItemResponse;
import com.ecommerce.ecommerce_backend.dto.CartResponse;
import com.ecommerce.ecommerce_backend.model.User;
import com.ecommerce.ecommerce_backend.service.CartService;
import com.ecommerce.ecommerce_backend.service.UserService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;

/**
 * Cart Controller
 * Handles shopping cart operations
 *
 * AUTHENTICATED endpoints (user must be logged in):
 * - GET /api/cart - View cart
 * - POST /api/cart/items - Add item to cart
 * - PUT /api/cart/items/{productId} - Update item quantity
 * - DELETE /api/cart/items/{productId} - Remove item from cart
 * - DELETE /api/cart - Clear cart
 *
 * NOTE: In a real app with JWT, we'd extract User from SecurityContext
 * For now, we'll use a placeholder
 */
@RestController
@RequestMapping("/api/cart")
@RequiredArgsConstructor
@Slf4j
public class CartController {

    private final CartService cartService;
    private final UserService userService;

    /**
     * Get current user from JWT token
     * Extracts email from SecurityContext and loads user from database
     */
    private User getCurrentUser() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        String email = (String) authentication.getPrincipal();
        return userService.getUserByEmail(email);
    }

    /**
     * Get user's cart
     * Endpoint: GET /api/cart
     * Response: 200 OK with cart details
     */
    @GetMapping
    public ResponseEntity<ApiResponse<CartResponse>> getCart() {
        log.info("Fetching cart");

        try {
            User user = getCurrentUser();
            CartResponse cart = cartService.getCart(user);

            ApiResponse<CartResponse> response = ApiResponse.success(
                    "Cart fetched successfully",
                    cart
            );

            return ResponseEntity.ok(response);

        } catch (RuntimeException e) {
            log.error("Error fetching cart", e);

            ApiResponse<CartResponse> errorResponse = ApiResponse.error(
                    "Failed to fetch cart",
                    e.getMessage()
            );

            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(errorResponse);

        } catch (Exception e) {
            log.error("Unexpected error", e);

            ApiResponse<CartResponse> errorResponse = ApiResponse.error(
                    "Failed to fetch cart",
                    "An unexpected error occurred"
            );

            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(errorResponse);
        }
    }

    /**
     * Add item to cart (or increase quantity if exists)
     * Endpoint: POST /api/cart/items
     * Body: {
     *   "productId": 1,
     *   "quantity": 2
     * }
     * Response: 201 Created with cart item details
     */
    @PostMapping("/items")
    public ResponseEntity<ApiResponse<CartItemResponse>> addItemToCart(
            @RequestBody CartItemRequest request) {
        log.info("Adding item to cart. ProductId: {}, Quantity: {}",
                request.getProductId(), request.getQuantity());

        try {
            User user = getCurrentUser();
            CartItemResponse cartItem = cartService.addItemToCart(user, request);

            ApiResponse<CartItemResponse> response = ApiResponse.success(
                    "Item added to cart successfully",
                    cartItem
            );

            return ResponseEntity.status(HttpStatus.CREATED).body(response);

        } catch (IllegalArgumentException e) {
            log.error("Invalid request: {}", e.getMessage());

            ApiResponse<CartItemResponse> errorResponse = ApiResponse.error(
                    "Invalid request",
                    e.getMessage()
            );

            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(errorResponse);

        } catch (RuntimeException e) {
            log.error("Error adding item to cart", e);

            ApiResponse<CartItemResponse> errorResponse = ApiResponse.error(
                    "Failed to add item",
                    e.getMessage()
            );

            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(errorResponse);

        } catch (Exception e) {
            log.error("Unexpected error", e);

            ApiResponse<CartItemResponse> errorResponse = ApiResponse.error(
                    "Failed to add item",
                    "An unexpected error occurred"
            );

            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(errorResponse);
        }
    }

    /**
     * Update item quantity in cart
     * Endpoint: PUT /api/cart/items/{productId}
     * Body: {
     *   "productId": 1,
     *   "quantity": 5
     * }
     * Response: 200 OK with updated cart item
     */
    @PutMapping("/items/{productId}")
    public ResponseEntity<ApiResponse<CartItemResponse>> updateItemQuantity(
            @PathVariable Long productId,
            @RequestBody CartItemRequest request) {
        log.info("Updating item quantity. ProductId: {}, New Quantity: {}",
                productId, request.getQuantity());

        try {
            User user = getCurrentUser();
            CartItemResponse cartItem = cartService.updateItemQuantity(
                    user, productId, request.getQuantity());

            ApiResponse<CartItemResponse> response = ApiResponse.success(
                    "Item quantity updated successfully",
                    cartItem
            );

            return ResponseEntity.ok(response);

        } catch (IllegalArgumentException e) {
            log.error("Invalid quantity: {}", e.getMessage());

            ApiResponse<CartItemResponse> errorResponse = ApiResponse.error(
                    "Invalid quantity",
                    e.getMessage()
            );

            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(errorResponse);

        } catch (RuntimeException e) {
            log.error("Item not found", e);

            ApiResponse<CartItemResponse> errorResponse = ApiResponse.error(
                    "Item not found in cart",
                    e.getMessage()
            );

            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(errorResponse);

        } catch (Exception e) {
            log.error("Error updating item", e);

            ApiResponse<CartItemResponse> errorResponse = ApiResponse.error(
                    "Failed to update item",
                    "An unexpected error occurred"
            );

            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(errorResponse);
        }
    }

    /**
     * Remove item from cart
     * Endpoint: DELETE /api/cart/items/{productId}
     * Response: 200 OK with success message
     */
    @DeleteMapping("/items/{productId}")
    public ResponseEntity<ApiResponse<String>> removeItemFromCart(@PathVariable Long productId) {
        log.info("Removing item from cart. ProductId: {}", productId);

        try {
            User user = getCurrentUser();
            cartService.removeItemFromCart(user, productId);

            ApiResponse<String> response = ApiResponse.success(
                    "Item removed from cart successfully"
            );

            return ResponseEntity.ok(response);

        } catch (RuntimeException e) {
            log.error("Error removing item", e);

            ApiResponse<String> errorResponse = ApiResponse.error(
                    "Failed to remove item",
                    e.getMessage()
            );

            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(errorResponse);

        } catch (Exception e) {
            log.error("Unexpected error", e);

            ApiResponse<String> errorResponse = ApiResponse.error(
                    "Failed to remove item",
                    "An unexpected error occurred"
            );

            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(errorResponse);
        }
    }

    /**
     * Clear entire cart
     * Endpoint: DELETE /api/cart
     * Response: 200 OK with success message
     */
    @DeleteMapping
    public ResponseEntity<ApiResponse<String>> clearCart() {
        log.info("Clearing cart");

        try {
            User user = getCurrentUser();
            cartService.clearCart(user);

            ApiResponse<String> response = ApiResponse.success(
                    "Cart cleared successfully"
            );

            return ResponseEntity.ok(response);

        } catch (RuntimeException e) {
            log.error("Error clearing cart", e);

            ApiResponse<String> errorResponse = ApiResponse.error(
                    "Failed to clear cart",
                    e.getMessage()
            );

            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(errorResponse);

        } catch (Exception e) {
            log.error("Unexpected error", e);

            ApiResponse<String> errorResponse = ApiResponse.error(
                    "Failed to clear cart",
                    "An unexpected error occurred"
            );

            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(errorResponse);
        }
    }
}