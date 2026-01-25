package com.ecommerce.ecommerce_backend.service;

import com.ecommerce.ecommerce_backend.dto.OrderCreateRequest;
import com.ecommerce.ecommerce_backend.dto.OrderResponse;
import com.ecommerce.ecommerce_backend.model.Cart;
import com.ecommerce.ecommerce_backend.model.CartItem;
import com.ecommerce.ecommerce_backend.model.Order;
import com.ecommerce.ecommerce_backend.model.OrderItem;
import com.ecommerce.ecommerce_backend.model.Product;
import com.ecommerce.ecommerce_backend.model.User;
import com.ecommerce.ecommerce_backend.repository.CartRepository;
import com.ecommerce.ecommerce_backend.repository.OrderItemRepository;
import com.ecommerce.ecommerce_backend.repository.OrderRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import java.util.List;
import java.util.stream.Collectors;

/**
 * Service for order operations
 *
 * Flow:
 * 1. User adds items to cart
 * 2. User checkout -> create order from cart
 * 3. Move cart items to order items
 * 4. Clear cart
 * 5. Reduce product quantities
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class OrderService {

    private final OrderRepository orderRepository;
    private final OrderItemRepository orderItemRepository;
    private final CartRepository cartRepository;
    private final ProductService productService;

    /**
     * Create order from user's cart
     *
     * Steps:
     * 1. Get user's cart
     * 2. Validate cart is not empty
     * 3. Create order with shipping details
     * 4. Create order items from cart items
     * 5. Reduce product quantities
     * 6. Clear cart
     */
    @Transactional
    public OrderResponse createOrder(User user, OrderCreateRequest request) {
        log.info("Creating order for user: {}", user.getId());

        // Get user's cart
        Cart cart = cartRepository.findByUser(user)
                .orElseThrow(() -> new RuntimeException("Cart not found"));

        // Validate cart is not empty
        if (cart.getCartItems() == null || cart.getCartItems().isEmpty()) {
            throw new IllegalArgumentException("Cart is empty. Cannot create order");
        }

        // Validate shipping details
        if (request.getShippingAddress() == null || request.getShippingAddress().trim().isEmpty()) {
            throw new IllegalArgumentException("Shipping address is required");
        }

        if (request.getPhoneNumber() == null || request.getPhoneNumber().trim().isEmpty()) {
            throw new IllegalArgumentException("Phone number is required");
        }

        // Calculate total price
        Double totalPrice = cart.getCartItems().stream()
                .mapToDouble(item -> item.getPrice() * item.getQuantity())
                .sum();

        // Create order
        Order order = Order.builder()
                .user(user)
                .totalPrice(totalPrice)
                .status(Order.OrderStatus.PENDING)
                .shippingAddress(request.getShippingAddress())
                .phoneNumber(request.getPhoneNumber())
                .build();

        Order savedOrder = orderRepository.save(order);
        log.info("Order created with id: {}", savedOrder.getId());

        // Create order items from cart items
        for (CartItem cartItem : cart.getCartItems()) {
            OrderItem orderItem = OrderItem.builder()
                    .order(savedOrder)
                    .product(cartItem.getProduct())
                    .quantity(cartItem.getQuantity())
                    .price(cartItem.getPrice())
                    .build();

            orderItemRepository.save(orderItem);

            // Reduce product quantity
            productService.reduceProductQuantity(
                    cartItem.getProduct().getId(),
                    cartItem.getQuantity()
            );

            log.info("Created order item for product: {}", cartItem.getProduct().getId());
        }

        // Clear cart
        cartRepository.delete(cart);
        log.info("Cart cleared after order creation");

        return OrderResponse.fromEntity(savedOrder);
    }

    /**
     * Get all orders of a user     *  should only see their own orders
     */
    public List<OrderResponse> getUserOrders(User user) {
        log.info("Fetching orders for user: {}", user.getId());

        List<Order> orders = orderRepository.findByUser(user);
        return orders.stream()
                .map(OrderResponse::fromEntity)
                .collect(Collectors.toList());
    }

    /**
     * Get order by ID (with security check)
     * User can only access their own orders
     */
    public OrderResponse getOrderById(Long orderId, User user) {
        Order order = orderRepository.findByIdAndUser(orderId, user)
                .orElseThrow(() -> new RuntimeException(
                        "Order not found or you don't have permission to access it"));

        return OrderResponse.fromEntity(order);
    }

    /**
     * Get all orders (ADMIN ONLY)
     */
    public List<OrderResponse> getAllOrders() {
        log.info("ADMIN: Fetching all orders");

        List<Order> orders = orderRepository.findAll();
        return orders.stream()
                .map(OrderResponse::fromEntity)
                .collect(Collectors.toList());
    }

    /**
     * Get orders by status (ADMIN)
     */
    public List<OrderResponse> getOrdersByStatus(Order.OrderStatus status) {
        log.info("ADMIN: Fetching orders with status: {}", status);

        List<Order> orders = orderRepository.findByStatus(status);
        return orders.stream()
                .map(OrderResponse::fromEntity)
                .collect(Collectors.toList());
    }

    /**
     * Update order status (ADMIN ONLY)
     */
    @Transactional
    public OrderResponse updateOrderStatus(Long orderId, Order.OrderStatus newStatus) {
        log.info("Updating order status. Order: {}, New Status: {}", orderId, newStatus);

        Order order = orderRepository.findById(orderId)
                .orElseThrow(() -> new RuntimeException("Order not found"));

        order.setStatus(newStatus);
        Order updatedOrder = orderRepository.save(order);

        log.info("Order status updated");
        return OrderResponse.fromEntity(updatedOrder);
    }

    /**
     * Cancel order
     * Only PENDING orders can be cancelled
     */
    @Transactional
    public OrderResponse cancelOrder(Long orderId, User user) {
        log.info("Cancelling order: {}", orderId);

        Order order = orderRepository.findByIdAndUser(orderId, user)
                .orElseThrow(() -> new RuntimeException("Order not found"));

        if (order.getStatus() != Order.OrderStatus.PENDING) {
            throw new IllegalArgumentException("Only PENDING orders can be cancelled");
        }

        // Restore product quantities
        for (OrderItem item : order.getOrderItems()) {
            productService.reduceProductQuantity(
                    item.getProduct().getId(),
                    -item.getQuantity() // Negative to increase
            );
        }

        order.setStatus(Order.OrderStatus.CANCELLED);
        Order cancelledOrder = orderRepository.save(order);

        log.info("Order cancelled");
        return OrderResponse.fromEntity(cancelledOrder);
    }

    /**
     * Get total orders count (ADMIN)
     */
    public Long getTotalOrdersCount() {
        return (long) orderRepository.findAll().size();
    }

    /**
     * Get total revenue (ADMIN)
     */
    public Double getTotalRevenue() {
        return orderRepository.findAll().stream()
                .filter(order -> order.getStatus() == Order.OrderStatus.DELIVERED)
                .mapToDouble(Order::getTotalPrice)
                .sum();
    }
}