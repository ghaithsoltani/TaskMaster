package com.taskmaster.repository;

import com.taskmaster.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

/**
 * Repository for User entity.
 *
 * Spring Data JPA automatically implements:
 * - save(), findById(), findAll(), deleteById(), etc.
 *
 * We add custom finder methods by following naming conventions.
 */
@Repository
public interface UserRepository extends JpaRepository<User, Integer> {

    /**
     * Find user by username.
     * Spring Data JPA generates: SELECT * FROM users WHERE username = ?
     */
    Optional<User> findByUsername(String username);

    /**
     * Find user by email.
     */
    Optional<User> findByEmail(String email);

    /**
     * Check if username exists.
     */
    boolean existsByUsername(String username);

    /**
     * Check if email exists.
     */
    boolean existsByEmail(String email);
}