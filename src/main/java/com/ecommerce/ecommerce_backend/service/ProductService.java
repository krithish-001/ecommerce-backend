package com.ecommerce.ecommerce_backend.service;

import com.ecommerce.ecommerce_backend.dto.ProductCreateRequest;
import com.ecommerce.ecommerce_backend.dto.ProductResponse;
import com.ecommerce.ecommerce_backend.model.Product;
import com.ecommerce.ecommerce_backend.repository.ProductRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import java.util.List;
import java.util.stream.Collectors;

/**
 * Service for product operations
 * Only ADMIN can create/update/delete products
 * Users can only view products
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class ProductService {

    private final ProductRepository productRepository;

    /**
     * Create new product
     * ADMIN ONLY
     */
    @Transactional
    public ProductResponse createProduct(ProductCreateRequest request) {
        log.info("Creating product: {}", request.getName());

        // Validate price
        if (request.getPrice() < 0) {
            throw new IllegalArgumentException("Price cannot be negative");
        }

        // Validate quantity
        if (request.getQuantity() < 0) {
            throw new IllegalArgumentException("Quantity cannot be negative");
        }

        Product product = Product.builder()
                .name(request.getName())
                .description(request.getDescription())
                .price(request.getPrice())
                .quantity(request.getQuantity())
                .category(request.getCategory())
                .imageUrl(request.getImageUrl())
                .isActive(true)
                .build();

        Product savedProduct = productRepository.save(product);
        log.info("Product created with id: {}", savedProduct.getId());

        return ProductResponse.fromEntity(savedProduct);
    }

    /**
     * Update existing product
     * ADMIN ONLY
     */
    @Transactional
    public ProductResponse updateProduct(Long productId, ProductCreateRequest request) {
        log.info("Updating product with id: {}", productId);

        Product product = productRepository.findById(productId)
                .orElseThrow(() -> new RuntimeException("Product not found with id: " + productId));

        // Update fields
        product.setName(request.getName());
        product.setDescription(request.getDescription());
        product.setPrice(request.getPrice());
        product.setQuantity(request.getQuantity());
        product.setCategory(request.getCategory());
        product.setImageUrl(request.getImageUrl());

        Product updatedProduct = productRepository.save(product);
        log.info("Product updated with id: {}", productId);

        return ProductResponse.fromEntity(updatedProduct);
    }

    /**
     * Delete product (soft delete - mark as inactive)
     * ADMIN ONLY
     *
     * WHY soft delete?
     * - Don't lose historical data
     * - Orders still reference this product
     * - Can restore if needed
     */
    @Transactional
    public void deleteProduct(Long productId) {
        log.info("Deleting product with id: {}", productId);

        Product product = productRepository.findById(productId)
                .orElseThrow(() -> new RuntimeException("Product not found with id: " + productId));

        product.setIsActive(false);
        productRepository.save(product);

        log.info("Product marked as inactive with id: {}", productId);
    }

    /**
     * Get all active products
     * PUBLIC - Users can view
     */
    public List<ProductResponse> getAllProducts() {
        log.info("Fetching all active products");
        return productRepository.findByIsActiveTrue()
                .stream()
                .map(ProductResponse::fromEntity)
                .collect(Collectors.toList());
    }

    /**
     * Get product by ID
     */
    public ProductResponse getProductById(Long productId) {
        Product product = productRepository.findByIdAndIsActiveTrue(productId)
                .orElseThrow(() -> new RuntimeException("Product not found with id: " + productId));
        return ProductResponse.fromEntity(product);
    }

    /**
     * Get products by category
     */
    public List<ProductResponse> getProductsByCategory(String category) {
        log.info("Fetching products in category: {}", category);
        return productRepository.findBycategoryAndIsActiveTrue(category)
                .stream()
                .map(ProductResponse::fromEntity)
                .collect(Collectors.toList());
    }

    /**
     * Get all categories
     */
    public List<String> getAllCategories() {
        return productRepository.findAllActiveCategories();
    }

    /**
     * Get product entity (internal use only)
     * Used by CartService and OrderService
     */
    public Product getProductEntityById(Long productId) {
        return productRepository.findByIdAndIsActiveTrue(productId)
                .orElseThrow(() -> new RuntimeException("Product not found with id: " + productId));
    }

    /**
     * Check product has enough quantity in stock
     */
    public boolean hasEnoughStock(Long productId, Integer requiredQuantity) {
        Product product = productRepository.findById(productId)
                .orElseThrow(() -> new RuntimeException("Product not found"));
        return product.getQuantity() >= requiredQuantity;
    }

    /**
     * Reduce product quantity after order
     * INTERNAL USE
     */
    @Transactional
    public void reduceProductQuantity(Long productId, Integer quantity) {
        Product product = productRepository.findById(productId)
                .orElseThrow(() -> new RuntimeException("Product not found"));

        product.setQuantity(product.getQuantity() - quantity);
        productRepository.save(product);

        log.info("Product quantity reduced. Id: {}, New Quantity: {}", productId, product.getQuantity());
    }
}