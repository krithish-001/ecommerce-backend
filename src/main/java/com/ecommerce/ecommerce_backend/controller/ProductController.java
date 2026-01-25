package com.ecommerce.ecommerce_backend.controller;

import com.ecommerce.ecommerce_backend.dto.ApiResponse;
import com.ecommerce.ecommerce_backend.dto.ProductCreateRequest;
import com.ecommerce.ecommerce_backend.dto.ProductResponse;
import com.ecommerce.ecommerce_backend.service.ProductService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import java.util.List;

/**
 * Product Controller
 * Handles product CRUD operations
 *
 * PUBLIC endpoints (no authentication needed):
 * - GET /api/products - View all products
 * - GET /api/products/{id} - View single product
 * - GET /api/products/category/{category} - View by category
 *
 * ADMIN endpoints (authentication required):
 * - POST /api/products - Create product
 * - PUT /api/products/{id} - Update product
 * - DELETE /api/products/{id} - Delete product
 */
@RestController
@RequestMapping("/api/products")
@RequiredArgsConstructor
@Slf4j
public class ProductController {

    private final ProductService productService;

    /**
     * Get all products
     * PUBLIC
     *
     * Endpoint: GET /api/products
     * Response: 200 OK with list of products
     */
    @GetMapping
    public ResponseEntity<ApiResponse<List<ProductResponse>>> getAllProducts() {
        log.info("Fetching all products");

        try {
            List<ProductResponse> products = productService.getAllProducts();

            ApiResponse<List<ProductResponse>> response = ApiResponse.success(
                    "Products fetched successfully",
                    products
            );

            return ResponseEntity.ok(response);

        } catch (Exception e) {
            log.error("Error fetching products", e);

            ApiResponse<List<ProductResponse>> errorResponse = ApiResponse.error(
                    "Failed to fetch products",
                    e.getMessage()
            );

            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(errorResponse);
        }
    }

    /**
     * Get product by ID
     * PUBLIC
     *
     * Endpoint: GET /api/products/{id}
     * Response: 200 OK with product details
     */
    @GetMapping("/{id}")
    public ResponseEntity<ApiResponse<ProductResponse>> getProductById(@PathVariable Long id) {
        log.info("Fetching product with id: {}", id);

        try {
            ProductResponse product = productService.getProductById(id);

            ApiResponse<ProductResponse> response = ApiResponse.success(
                    "Product fetched successfully",
                    product
            );

            return ResponseEntity.ok(response);

        } catch (RuntimeException e) {
            log.error("Product not found with id: {}", id);

            ApiResponse<ProductResponse> errorResponse = ApiResponse.error(
                    "Product not found",
                    e.getMessage()
            );

            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(errorResponse);

        } catch (Exception e) {
            log.error("Error fetching product", e);

            ApiResponse<ProductResponse> errorResponse = ApiResponse.error(
                    "Failed to fetch product",
                    e.getMessage()
            );

            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(errorResponse);
        }
    }

    /**
     * Get products by category
     * PUBLIC
     *
     * Endpoint: GET /api/products/category/{category}
     * Response: 200 OK with products in category
     */
    @GetMapping("/category/{category}")
    public ResponseEntity<ApiResponse<List<ProductResponse>>> getProductsByCategory(
            @PathVariable String category) {
        log.info("Fetching products in category: {}", category);

        try {
            List<ProductResponse> products = productService.getProductsByCategory(category);

            ApiResponse<List<ProductResponse>> response = ApiResponse.success(
                    "Products in category fetched successfully",
                    products
            );

            return ResponseEntity.ok(response);

        } catch (Exception e) {
            log.error("Error fetching products by category", e);

            ApiResponse<List<ProductResponse>> errorResponse = ApiResponse.error(
                    "Failed to fetch products",
                    e.getMessage()
            );

            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(errorResponse);
        }
    }

    /**
     * Get all categories
     * PUBLIC
     *
     * Endpoint: GET /api/products/categories/all
     * Response: 200 OK with list of categories
     */
    @GetMapping("/categories/all")
    public ResponseEntity<ApiResponse<List<String>>> getAllCategories() {
        log.info("Fetching all categories");

        try {
            List<String> categories = productService.getAllCategories();

            ApiResponse<List<String>> response = ApiResponse.success(
                    "Categories fetched successfully",
                    categories
            );

            return ResponseEntity.ok(response);

        } catch (Exception e) {
            log.error("Error fetching categories", e);

            ApiResponse<List<String>> errorResponse = ApiResponse.error(
                    "Failed to fetch categories",
                    e.getMessage()
            );

            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(errorResponse);
        }
    }

    /**
     * Create product
     * ADMIN ONLY
     *
     * Endpoint: POST /api/products
     * Body: {
     *   "name": "Product Name",
     *   "description": "Description",
     *   "price": 99.99,
     *   "quantity": 100,
     *   "category": "Electronics",
     *   "imageUrl": "http://..."
     * }
     * Response: 201 Created with product details
     *
     * TODO: Add @PreAuthorize("hasRole('ADMIN')")
     */
    @PostMapping
    public ResponseEntity<ApiResponse<ProductResponse>> createProduct(@RequestBody ProductCreateRequest request) {
        log.info("Creating product: {}", request.getName());

        try {
            ProductResponse product = productService.createProduct(request);

            ApiResponse<ProductResponse> response = ApiResponse.success(
                    "Product created successfully",
                    product
            );

            return ResponseEntity.status(HttpStatus.CREATED).body(response);

        } catch (IllegalArgumentException e) {
            log.error("Invalid product data: {}", e.getMessage());

            ApiResponse<ProductResponse> errorResponse = ApiResponse.error(
                    "Invalid product data",
                    e.getMessage()
            );

            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(errorResponse);

        } catch (Exception e) {
            log.error("Error creating product", e);

            ApiResponse<ProductResponse> errorResponse = ApiResponse.error(
                    "Failed to create product",
                    e.getMessage()
            );

            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(errorResponse);
        }
    }

    /**
     * Update product
     * ADMIN ONLY
     *
     * Endpoint: PUT /api/products/{id}
     * Body: {
     *   "name": "Updated Name",
     *   "description": "Updated Description",
     *   "price": 79.99,
     *   "quantity": 50,
     *   "category": "Electronics",
     *   "imageUrl": "http://..."
     * }
     * Response: 200 OK with updated product
     *
     * TODO: Add @PreAuthorize("hasRole('ADMIN')")
     */
    @PutMapping("/{id}")
    public ResponseEntity<ApiResponse<ProductResponse>> updateProduct(
            @PathVariable Long id,
            @RequestBody ProductCreateRequest request) {
        log.info("Updating product with id: {}", id);

        try {
            ProductResponse product = productService.updateProduct(id, request);

            ApiResponse<ProductResponse> response = ApiResponse.success(
                    "Product updated successfully",
                    product
            );

            return ResponseEntity.ok(response);

        } catch (RuntimeException e) {
            log.error("Product not found with id: {}", id);

            ApiResponse<ProductResponse> errorResponse = ApiResponse.error(
                    "Product not found",
                    e.getMessage()
            );

            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(errorResponse);

        } catch (Exception e) {
            log.error("Error updating product", e);

            ApiResponse<ProductResponse> errorResponse = ApiResponse.error(
                    "Failed to update product",
                    e.getMessage()
            );

            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(errorResponse);
        }
    }

    /**
     * Delete product
     * ADMIN ONLY
     *
     * Endpoint: DELETE /api/products/{id}
     * Response: 200 OK with success message
     *
     * Note: Soft delete - marks as inactive, doesn't actually delete
     *
     * TODO: Add @PreAuthorize("hasRole('ADMIN')")
     */
    @DeleteMapping("/{id}")
    public ResponseEntity<ApiResponse<String>> deleteProduct(@PathVariable Long id) {
        log.info("Deleting product with id: {}", id);

        try {
            productService.deleteProduct(id);

            ApiResponse<String> response = ApiResponse.success(
                    "Product deleted successfully"
            );

            return ResponseEntity.ok(response);

        } catch (RuntimeException e) {
            log.error("Product not found with id: {}", id);

            ApiResponse<String> errorResponse = ApiResponse.error(
                    "Product not found",
                    e.getMessage()
            );

            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(errorResponse);

        } catch (Exception e) {
            log.error("Error deleting product", e);

            ApiResponse<String> errorResponse = ApiResponse.error(
                    "Failed to delete product",
                    e.getMessage()
            );

            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(errorResponse);
        }
    }
}