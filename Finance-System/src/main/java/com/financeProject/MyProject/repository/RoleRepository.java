package com.financeProject.MyProject.repository;

import com.financeProject.MyProject.model.Role;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

/*
  Repository interface for Role entity operations.
  Provides database access for role management and lookups.
 */

@Repository
public interface RoleRepository extends JpaRepository<Role, Long> {

    // Custom method to find role by name (VIEWER, ANALYST, ADMIN)
    Optional<Role> findByName(String name);
}
