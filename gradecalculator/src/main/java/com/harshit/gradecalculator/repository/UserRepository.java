package com.harshit.gradecalculator.repository;

import com.harshit.gradecalculator.model.User;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.Optional;

// JpaRepository<Entity, ID_Type>
public interface UserRepository extends JpaRepository<User, Integer> {
    
    // Spring Boot automatically implements this!
    // It generates SQL: "SELECT * FROM Users WHERE username = ?"
    Optional<User> findByUsername(String username);
    
    // Checks if an email exists
    boolean existsByEmail(String email);
}