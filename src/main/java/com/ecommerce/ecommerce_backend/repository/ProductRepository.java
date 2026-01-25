package com.ecommerce.ecommerce_backend.repository;

import com.ecommerce.ecommerce_backend.model.Product;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import java.util.List;
import java.util.Optional;

/**
 * Repository for Product entity
 * Handles all product-related database queries
 */
@Repository
public interface ProductRepository extends JpaRepository<Product, Long> {

    /**
     * Find all active products
     * Used when users view product list
     */
    List<Product> findByIsActiveTrue();

    /**
     * Find products by category (active only)
     * Allows filtering products by type
     */
    List<Product> findBycategoryAndIsActiveTrue(String category);

    /**
     * Find product by name (case-insensitive)
     * Using @Query for custom SQL
     */
    @Query("SELECT p FROM Product p WHERE LOWER(p.name) = LOWER(:name) AND p.isActive = true")
    Optional<Product> findByNameIgnoreCaseAndActive(@Param("name") String name);

    /**
     * Check if product exists and is active
     */
    boolean existsByIdAndIsActiveTrue(Long id);

    /**
     * Find product by ID (must be active)
     */
    Optional<Product> findByIdAndIsActiveTrue(Long id);

    /**
     * Get all distinct categories (for filtering)
     */
    @Query("SELECT DISTINCT p.category FROM Product p WHERE p.isActive = true")
    List<String> findAllActiveCategories();
}