package com.financeProject.MyProject.model;

import com.fasterxml.jackson.annotation.JsonFormat;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;
import java.time.LocalDateTime;

/*
  Entity representing a financial transaction record in the system.

  This entity maps to the "financial_records" table and stores all financial
  transactions including income and expenses for each user in the system.

  Core Features:
  - Supports both INCOME and EXPENSE transaction types
  - Implements soft delete mechanism for data recovery
  - Maintains audit timestamps for tracking
  - Links to User entity for ownership management

  Role-Based Access Control Integration:
  Financial records are accessed based on user roles:
    • VIEWER  → Can only access records where user_id = their ID
    • ANALYST → Can access ALL records (read-only)
    • ADMIN   → Can access ALL records with full CRUD operations

  Transaction Types:
  - INCOME:  Money received (salary, freelance, gifts, refunds)
  - EXPENSE: Money spent (rent, groceries, utilities, entertainment)

   Soft Delete Strategy:
  - deleted = false → Record is active and visible in queries
  - deleted = true  → Record is hidden from normal queries
  - Benefits: Data recovery, audit trails, historical reporting

  Audit Fields:
  - createdAt: Automatically set when record is first persisted
  - recordDate: Business date of the transaction (can be different from createdAt)

  Database Constraints:
  - user_id: Foreign key to users table (NOT NULL)
  - amount: Must be positive (enforced at application level)
  - type: Only 'INCOME' or 'EXPENSE' allowed
  - recordDate: Defaults to current date if not provided

  @see User
  @see FinancialRecordService
  @see FinancialRecordController
 */

@Data
@NoArgsConstructor
@AllArgsConstructor
@Entity
@Table(name = "financial_records")
public class FinancialRecord {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    @Column(nullable = false)
    private Double amount;

    @Column(nullable = false)
    private String type; // INCOME / EXPENSE

    private String category;

    @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "yyyy-MM-dd")
    @Column(nullable = false)
    private LocalDate recordDate;

    private String notes;

    private LocalDateTime createdAt;

    @PrePersist
    public void prePersist() {
        this.deleted = false;

        if (this.recordDate == null) {
            this.recordDate = LocalDate.now();
        }
    }

    @Column(nullable = false)
    private Boolean deleted = false; // used for soft delete


}