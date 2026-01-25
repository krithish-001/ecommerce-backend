package com.ecommerce.ecommerce_backend.controller;

import com.ecommerce.ecommerce_backend.dto.ApiResponse;
import com.ecommerce.ecommerce_backend.dto.OrderCreateRequest;
import com.ecommerce.ecommerce_backend.dto.OrderResponse;
import com.ecommerce.ecommerce_backend.model.Order;
import com.ecommerce.ecommerce_backend.model.User;
import com.ecommerce.ecommerce_backend.service.OrderService;
import com.ecommerce.ecommerce_backend.service.UserService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;
import java.util.List;

/**
 * Order Controller
 * Handles order operations
 *
 * USER endpoints (authenticated):
 * - POST /api/orders - Create order from cart
 * - GET /api/orders - Get user's orders
 * - GET /api/orders/{id} - Get specific order
 * - DELETE /api/orders/{id} - Cancel order
 *
 * ADMIN endpoints (admin only):
 * - GET /api/orders/all - Get all orders
 * - GET /api/orders/status/{status} - Get by status
 * - PUT /api/orders/{id}/status - Update status
 */
@RestController
@RequestMapping("/api/orders")
@RequiredArgsConstructor
@Slf4j
public class OrderController {

    private final OrderService orderService;
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
     * Create order from user's cart
     * Endpoint: POST /api/orders
     * Body: {
     *   "shippingAddress": "123 Main St, City",
     *   "phoneNumber": "1234567890"
     * }
     * Response: 201 Created with order details
     */
    @PostMapping
    public ResponseEntity<ApiResponse<OrderResponse>> createOrder(
            @RequestBody OrderCreateRequest request) {
        log.info("Creating order");

        try {
            User user = getCurrentUser();
            OrderResponse order = orderService.createOrder(user, request);

            ApiResponse<OrderResponse> response = ApiResponse.success(
                    "Order created successfully",
                    order
            );

            return ResponseEntity.status(HttpStatus.CREATED).body(response);

        } catch (IllegalArgumentException e) {
            log.error("Invalid order data: {}", e.getMessage());

            ApiResponse<OrderResponse> errorResponse = ApiResponse.error(
                    "Invalid order data",
                    e.getMessage()
            );

            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(errorResponse);

        } catch (RuntimeException e) {
            log.error("Error creating order", e);

            ApiResponse<OrderResponse> errorResponse = ApiResponse.error(
                    "Failed to create order",
                    e.getMessage()
            );

            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(errorResponse);

        } catch (Exception e) {
            log.error("Unexpected error", e);

            ApiResponse<OrderResponse> errorResponse = ApiResponse.error(
                    "Failed to create order",
                    "An unexpected error occurred"
            );

            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(errorResponse);
        }
    }

    /**
     * Get user's orders
     * Endpoint: GET /api/orders
     * Response: 200 OK with list of user's orders
     */
    @GetMapping
    public ResponseEntity<ApiResponse<List<OrderResponse>>> getUserOrders() {
        log.info("Fetching user orders");

        try {
            User user = getCurrentUser();
            List<OrderResponse> orders = orderService.getUserOrders(user);

            ApiResponse<List<OrderResponse>> response = ApiResponse.success(
                    "Orders fetched successfully",
                    orders
            );

            return ResponseEntity.ok(response);

        } catch (Exception e) {
            log.error("Error fetching orders", e);

            ApiResponse<List<OrderResponse>> errorResponse = ApiResponse.error(
                    "Failed to fetch orders",
                    e.getMessage()
            );

            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(errorResponse);
        }
    }

    /**
     * Get specific order by ID
     * Endpoint: GET /api/orders/{id}
     * Response: 200 OK with order details
     *
     * Security: User can only access their own orders
     */
    @GetMapping("/{id}")
    public ResponseEntity<ApiResponse<OrderResponse>> getOrderById(@PathVariable Long id) {
        log.info("Fetching order with id: {}", id);

        try {
            User user = getCurrentUser();
            OrderResponse order = orderService.getOrderById(id, user);

            ApiResponse<OrderResponse> response = ApiResponse.success(
                    "Order fetched successfully",
                    order
            );

            return ResponseEntity.ok(response);

        } catch (RuntimeException e) {
            log.error("Order not found or access denied", e);

            ApiResponse<OrderResponse> errorResponse = ApiResponse.error(
                    "Order not found or access denied",
                    e.getMessage()
            );

            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(errorResponse);

        } catch (Exception e) {
            log.error("Error fetching order", e);

            ApiResponse<OrderResponse> errorResponse = ApiResponse.error(
                    "Failed to fetch order",
                    e.getMessage()
            );

            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(errorResponse);
        }
    }

    /**
     * Cancel order
     * Endpoint: DELETE /api/orders/{id}
     * Response: 200 OK with cancelled order
     *
     * Note: Only PENDING orders can be cancelled
     */
    @DeleteMapping("/{id}")
    public ResponseEntity<ApiResponse<OrderResponse>> cancelOrder(@PathVariable Long id) {
        log.info("Cancelling order with id: {}", id);

        try {
            User user = getCurrentUser();
            OrderResponse order = orderService.cancelOrder(id, user);

            ApiResponse<OrderResponse> response = ApiResponse.success(
                    "Order cancelled successfully",
                    order
            );

            return ResponseEntity.ok(response);

        } catch (IllegalArgumentException e) {
            log.error("Cannot cancel order: {}", e.getMessage());

            ApiResponse<OrderResponse> errorResponse = ApiResponse.error(
                    "Cannot cancel order",
                    e.getMessage()
            );

            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(errorResponse);

        } catch (RuntimeException e) {
            log.error("Order not found", e);

            ApiResponse<OrderResponse> errorResponse = ApiResponse.error(
                    "Order not found",
                    e.getMessage()
            );

            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(errorResponse);

        } catch (Exception e) {
            log.error("Error cancelling order", e);

            ApiResponse<OrderResponse> errorResponse = ApiResponse.error(
                    "Failed to cancel order",
                    "An unexpected error occurred"
            );

            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(errorResponse);
        }
    }

    /**
     * Get all orders (ADMIN ONLY)
     * Endpoint: GET /api/orders/admin/all
     * Response: 200 OK with all orders
     *
     * TODO: Add @PreAuthorize("hasRole('ADMIN')")
     */
    @GetMapping("/admin/all")
    public ResponseEntity<ApiResponse<List<OrderResponse>>> getAllOrders() {
        log.info("ADMIN: Fetching all orders");

        try {
            List<OrderResponse> orders = orderService.getAllOrders();

            ApiResponse<List<OrderResponse>> response = ApiResponse.success(
                    "All orders fetched successfully",
                    orders
            );

            return ResponseEntity.ok(response);

        } catch (Exception e) {
            log.error("Error fetching all orders", e);

            ApiResponse<List<OrderResponse>> errorResponse = ApiResponse.error(
                    "Failed to fetch orders",
                    e.getMessage()
            );

            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(errorResponse);
        }
    }

    /**
     * Get orders by status (ADMIN ONLY)
     * Endpoint: GET /api/orders/admin/status/{status}
     * Example: GET /api/orders/admin/status/PENDING
     * Response: 200 OK with orders matching status
     *
     * TODO: Add @PreAuthorize("hasRole('ADMIN')")
     */
    @GetMapping("/admin/status/{status}")
    public ResponseEntity<ApiResponse<List<OrderResponse>>> getOrdersByStatus(@PathVariable String status) {
        log.info("ADMIN: Fetching orders with status: {}", status);

        try {
            Order.OrderStatus orderStatus = Order.OrderStatus.valueOf(status.toUpperCase());
            List<OrderResponse> orders = orderService.getOrdersByStatus(orderStatus);

            ApiResponse<List<OrderResponse>> response = ApiResponse.success(
                    "Orders fetched successfully",
                    orders
            );

            return ResponseEntity.ok(response);

        } catch (IllegalArgumentException e) {
            log.error("Invalid order status: {}", status);

            ApiResponse<List<OrderResponse>> errorResponse = ApiResponse.error(
                    "Invalid order status",
                    "Valid statuses: PENDING, CONFIRMED, SHIPPED, DELIVERED, CANCELLED"
            );

            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(errorResponse);

        } catch (Exception e) {
            log.error("Error fetching orders by status", e);

            ApiResponse<List<OrderResponse>> errorResponse = ApiResponse.error(
                    "Failed to fetch orders",
                    e.getMessage()
            );

            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(errorResponse);
        }
    }

    /**
     * Update order status (ADMIN ONLY)
     * Endpoint: PUT /api/orders/{id}/status
     * Body: {
     *   "status": "CONFIRMED"
     * }
     * Response: 200 OK with updated order
     *
     * TODO: Add @PreAuthorize("hasRole('ADMIN')")
     */
    @PutMapping("/{id}/status")
    public ResponseEntity<ApiResponse<OrderResponse>> updateOrderStatus(
            @PathVariable Long id,
            @RequestParam String status) {
        log.info("ADMIN: Updating order status. OrderId: {}, NewStatus: {}", id, status);

        try {
            Order.OrderStatus orderStatus = Order.OrderStatus.valueOf(status.toUpperCase());
            OrderResponse order = orderService.updateOrderStatus(id, orderStatus);

            ApiResponse<OrderResponse> response = ApiResponse.success(
                    "Order status updated successfully",
                    order
            );

            return ResponseEntity.ok(response);

        } catch (IllegalArgumentException e) {
            log.error("Invalid order status: {}", status);

            ApiResponse<OrderResponse> errorResponse = ApiResponse.error(
                    "Invalid order status",
                    "Valid statuses: PENDING, CONFIRMED, SHIPPED, DELIVERED, CANCELLED"
            );

            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(errorResponse);

        } catch (RuntimeException e) {
            log.error("Order not found", e);

            ApiResponse<OrderResponse> errorResponse = ApiResponse.error(
                    "Order not found",
                    e.getMessage()
            );

            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(errorResponse);

        } catch (Exception e) {
            log.error("Error updating order status", e);

            ApiResponse<OrderResponse> errorResponse = ApiResponse.error(
                    "Failed to update order",
                    "An unexpected error occurred"
            );

            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(errorResponse);
        }
    }
}