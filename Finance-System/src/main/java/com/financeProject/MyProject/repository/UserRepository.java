package com.financeProject.MyProject.repository;

import com.financeProject.MyProject.model.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

/*
  Repository interface for User entity operations.
  Provides database access for user authentication, management, and role-based queries.
 */

@Repository
public interface UserRepository extends JpaRepository<User, Long> {

    // Used for login or checking duplicate email
    Optional<User> findByEmail(String email);

    // Find all users with a specific role (e.g., VIEWER, ANALYST, ADMIN)
    List<User> findByRoleName(String roleName);
}