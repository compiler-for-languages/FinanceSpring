package com.financeProject.MyProject.model;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/*
  Entity representing user roles for role-based access control (RBAC).
  This entity maps to the "roles" table and defines the permission levels available in the system. Roles determine what actions a user can perform and what data they can access.

  System Roles Definition:
  VIEWER:  • View own dashboard summaries
           • View own transaction list
           • Cannot create, update, or delete any transactions
           • Cannot see other users' data
  ANALYST: • View own dashboard and transactions
           • View ALL users' transactions (read-only)
           • View company-wide analytics and trends
           • Export reports
           • Cannot create, update, or delete transactions
           • Cannot manage users
 ADMIN:    • Full system access
           • Create, read, update, delete ANY transaction
           • Create, read, update, delete users
           • Assign or change user roles
           • Activate/deactivate user accounts
           • View all system data and analytics

 Security Constraints:
  • ADMIN role cannot be created via API (security restriction)
  • ADMIN role cannot be assigned through user update endpoints
  • At least one ADMIN must exist in the system (created during setup)
  • Users cannot change their own role

  Database Design:
  • Roles are referenced by User entity (Many-to-One relationship)
  • Role names are unique to prevent duplicates
  • Role names are immutable once defined

  Typical Role Assignment Flow:
  1. System initialized with default ADMIN user
  2. ADMIN creates new users with VIEWER or ANALYST role
  3. ADMIN can promote ANALYST to higher roles as needed
  4. ADMIN can demote users for policy violations

 */

@Entity
@Table(name = "roles")
@Data
@AllArgsConstructor
@NoArgsConstructor

public class Role {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(unique = true, nullable = false)
    private String name; // VIEWER, ANALYST, ADMIN

}

/*
  Initial role data that should be loaded during system startup:

  INSERT INTO roles (name) VALUES ('VIEWER');
  INSERT INTO roles (name) VALUES ('ANALYST');
  INSERT INTO roles (name) VALUES ('ADMIN');

  These roles are required for the system to function properly.
  The default ADMIN user should be created with the ADMIN role ID.
 */