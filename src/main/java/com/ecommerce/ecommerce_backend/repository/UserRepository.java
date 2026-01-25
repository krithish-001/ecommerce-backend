package com.ecommerce.ecommerce_backend.repository;

import com.ecommerce.ecommerce_backend.model.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import java.util.Optional;

/**
 * Repository for User entity
 * JpaRepository provides CRUD operations + pagination
 *
 * WHY @Repository?
 * - Marks this as a data access component
 * - Spring auto-creates the implementation at runtime
 * - Allows Spring to manage transactions automatically
 */
@Repository
public interface UserRepository extends JpaRepository<User, Long> {

    /**
     * Find user by email
     * Spring generates SQL: SELECT * FROM users WHERE email = ?
     */
    Optional<User> findByEmail(String email);

    /**
     * Check if user exists by email
     * Returns true/false without fetching full user object (efficient)
     */
    boolean existsByEmail(String email);

    /**
     * Find active users only
     */
    Optional<User> findByEmailAndIsActiveTrue(String email);
}