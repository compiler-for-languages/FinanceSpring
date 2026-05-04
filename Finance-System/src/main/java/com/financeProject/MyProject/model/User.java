package com.financeProject.MyProject.model;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

/*
  Entity representing system users with authentication and authorization.

  This entity maps to the "users" table and stores all user account information including credentials, role assignments, and account status.

  Core Features:
  - User authentication (email + password)
  - Role-based access control (RBAC) via Role entity
  - Account status management (ACTIVE/INACTIVE)
  - Audit timestamp for account creation

  Account Status Definitions:
  ACTIVE : User can log in and access system based on their role
  INACTIVE : User cannot log in; account temporarily or permanently locked

 Security Constraints:
  • Password is stored encrypted using BCrypt algorithm
  • Email serves as unique username for authentication
  • ADMIN users cannot be deactivated or deleted via API
  • Users cannot modify their own role
  • At least one ADMIN must always exist in the system

 Audit Fields:
  • createdAt: Automatically set when user account is created
  • No updatedAt field (optional, can be added for audit trail)

Database Constraints:
  • email: Unique constraint, used as login username
  • role_id: Foreign key to roles table (NOT NULL)
  • status: Only 'ACTIVE' or 'INACTIVE' allowed

Typical User Lifecycle:
  1. ADMIN creates user account with VIEWER or ANALYST role
  2. User receives email with temporary credentials
  3. User logs in and changes password
  4. ADMIN may update role or status as needed
  5. ADMIN deactivates or deletes account when user leaves
 */

@Data
@AllArgsConstructor
@NoArgsConstructor
@Entity
@Table(name = "users")
public class User {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private String name;

    @Column(unique = true, nullable = false)
    private String email;

    @Column(nullable = false)
    private String password;

    @ManyToOne   // Many users can have same role
    @JoinColumn(name = "role_id", nullable = false)
    private Role role;

    @Column(nullable = false)
    private String status; // ACTIVE / INACTIVE

    @Getter
    private LocalDateTime createdAt;

    @PrePersist
    public void onCreate() {
        this.createdAt = LocalDateTime.now();
    }

}