package com.it342.g3.authentication.repository;

import com.it342.g3.authentication.model.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import java.util.Optional;

/**
 * User Repository - Authentication Slice
 * Data access layer for User entities
 * Vertical Slice: Authentication
 */
@Repository
public interface UserRepository extends JpaRepository<User, Long> {
    
    /**
     * Find user by email
     */
    Optional<User> findByEmail(String email);
    
    /**
     * Find user by username
     */
    Optional<User> findByUsername(String username);
    
    /**
     * Check if user exists by email
     */
    boolean existsByEmail(String email);
    
    /**
     * Check if user exists by username
     */
    boolean existsByUsername(String username);
}
