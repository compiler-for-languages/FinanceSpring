package com.financeProject.MyProject.repository;

import com.financeProject.MyProject.model.FinancialRecord;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

/*
 Repository interface for FinancialRecord entity operations.
 Provides CRUD operations and custom query methods for financial transactions.
 Supports soft delete filtering and pagination for efficient data retrieval.
 */

@Repository
public interface FinancialRecordRepository extends JpaRepository<FinancialRecord, Long> {

    // Get all records of a particular user
    List<FinancialRecord> findByUserId(Long userId);

    // Filter by type (INCOME / EXPENSE)
    List<FinancialRecord> findByType(String type);

    // Filter by category  (e.g., groceries, rent)
    List<FinancialRecord> findByCategory(String category);

    // Get all active transactions (soft delete filter)
    List<FinancialRecord> findByDeletedFalse();

    // Get active transactions for a specific user
    List<FinancialRecord> findByUserIdAndDeletedFalse(Long userId);

    // Paginated view of all active transactions
    Page<FinancialRecord> findByDeletedFalse(Pageable pageable);

    // Paginated view of active transactions for a specific user
    Page<FinancialRecord> findByUserIdAndDeletedFalse(Long userId, Pageable pageable);
}
