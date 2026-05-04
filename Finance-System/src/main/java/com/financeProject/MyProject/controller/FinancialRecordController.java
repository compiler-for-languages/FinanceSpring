package com.financeProject.MyProject.controller;

import com.financeProject.MyProject.dto.FinancialRecordRequestDTO;
import com.financeProject.MyProject.dto.FinancialRecordResponseDTO;
import com.financeProject.MyProject.service.FinancialRecordService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.security.Principal;
import java.util.List;


/*
     REST Controller for managing financial records (transactions).
     This controller provides comprehensive CRUD operations for financial transactions with role-based access control.
 */

@RestController
@RequestMapping("/records")
public class FinancialRecordController {

    @Autowired
    private FinancialRecordService recordService;

    /*
      Creates a new financial record for a specified user.
      Endpoint: POST /records/add?userId={userId}

      This endpoint allows ADMIN users to create transactions for any user in the system. Regular users (VIEWER/ANALYST) cannot create transactions.
      Security:
      - Only ADMIN role can access this endpoint
      - Service layer validates ADMIN privileges
      - Cannot create transactions for non-existent users

      @param userId - ID of the user for whom the transaction is being created
      @param requestDTO - FinancialRecordRequestDTO containing transaction details:
                       - amount: Transaction amount (positive number)
                        - type: "INCOME" or "EXPENSE"
                        - category: Category name (e.g., "salary", "rent")
                        - date: Transaction date (YYYY-MM-DD)
                        - description: Optional notes about the transaction
      @param principal Spring Security principal containing logged-in user's email

      @return FinancialRecordResponseDTO containing created transaction details
      @throws RuntimeException if:
              - Current user is not ADMIN
              - Target user doesn't exist
              - Invalid transaction data
              - Amount is negative or zero

     * @see FinancialRecordRequestDTO
     * @see FinancialRecordResponseDTO
     * @see FinancialRecordService#createRecord(String, Long, FinancialRecordRequestDTO)
     */
    @PostMapping("/add")
    public ResponseEntity<?> createRecord(
            @RequestParam Long userId,
            @RequestBody FinancialRecordRequestDTO requestDTO,
            Principal principal) {
        String adminEmail = principal.getName();
        FinancialRecordResponseDTO createdRecord = recordService.createRecord(adminEmail, userId, requestDTO);
        return ResponseEntity.status(HttpStatus.CREATED).body(createdRecord);
    }

    /*
      Retrieves all financial records based on user role.
      Endpoint: GET /records/all

      Role-Based Behavior:
      - VIEWER: Returns only their own transactions
      - ANALYST: Returns ALL users' transactions (read-only)
      - ADMIN: Returns ALL users' transactions (full access)

      @param principal Spring Security principal containing logged-in user's email
      @return List of FinancialRecordResponseDTO containing transactions
              Returns empty list if no transactions exist

      @throws RuntimeException if user not found

     * @see FinancialRecordService#getRecords(String)
     */

    @GetMapping("/all")
    public ResponseEntity<?> getAllRecords(Principal principal) {
        String email = principal.getName();
        List<FinancialRecordResponseDTO> records = recordService.getRecords(email);

        return (records.isEmpty())
                ? ResponseEntity.status(HttpStatus.OK).body("No records found. Table is empty.")
                : ResponseEntity.ok(records);
    }

    /*
      Retrieves a specific financial record by its ID.
      Endpoint: GET /records/{id}

      Access Control:
      - VIEWER: Can only access their own records
      - ANALYST: Can access any record (read-only)
      - ADMIN: Can access any record (full access)

      @param id The unique identifier of the financial record
      @param principal Spring Security principal containing logged-in user's email

      @return FinancialRecordResponseDTO containing the transaction details

      @throws RuntimeException if:
              - Record not found
              - VIEWER tries to access another user's record
              - User not authenticated

      @see FinancialRecordService#getRecordById(Long, String)
     */


    @GetMapping("/{id}")
    public ResponseEntity<?> getRecordById(@PathVariable Long id, Principal principal) {
        String email = principal.getName();
        FinancialRecordResponseDTO record = recordService.getRecordById(id, email);
        return ResponseEntity.ok(record);
    }

    /*
      Deletes ALL financial records from the system.
      Endpoint: DELETE /records/all

      Security:
      - Only ADMIN role can perform this operation
      - Should be protected with additional safeguards in production:
        - Confirmation dialog
        - Soft delete instead of hard delete
        - Audit logging
        - Backup before deletion

      @param principal Spring Security principal containing logged-in user's email
      @return String confirmation message
      @throws RuntimeException if current user is not ADMIN

      @see FinancialRecordService#deleteAllRecords(String)
     */
    @DeleteMapping("/all")
    public ResponseEntity<?> deleteAllRecords(Principal principal) {
        String email = principal.getName();
        recordService.deleteAllRecords(email);
        return ResponseEntity.ok("All records deleted successfully");
    }

    /*
      Deletes a specific financial record by its ID.
      Endpoint: DELETE /records/{id}

      Security:
      - Only ADMIN role can delete records
      - Cannot delete records that don't exist

      @param id The unique identifier of the record to delete
      @param principal Spring Security principal containing logged-in user's email

      @return String confirmation message
      @throws RuntimeException if:
              - Current user is not ADMIN
              - Record not found
              - Database error occurs
     * @see FinancialRecordService#deleteRecordById(Long, String)
     */
    @DeleteMapping("/{id}")
    public ResponseEntity<?> deleteRecordById(@PathVariable Long id, Principal principal) {
        String email = principal.getName();
        recordService.deleteRecordById(id, email);
        return ResponseEntity.ok("Record deleted successfully");
    }

    /*
      Retrieves filtered financial records based on criteria.
      Endpoint: GET /records/filter

      This endpoint allows filtering transactions by various criteria:
      - Transaction type (INCOME/EXPENSE)
      - Category (e.g., groceries, rent)
      - Date range (startDate to endDate)

      Role-Based Data Access:
      - VIEWER: Filters only their own transactions
      - ANALYST: Filters ALL users' transactions
      - ADMIN: Filters ALL users' transactions

      @param type Optional filter by transaction type ("INCOME" or "EXPENSE")
      @param category Optional filter by category name
      @param startDate Optional start date for date range (YYYY-MM-DD format)
      @param endDate Optional end date for date range (YYYY-MM-DD format)
      @param principal Spring Security principal containing logged-in user's email

      @return List of filtered FinancialRecordResponseDTO
              Returns empty list if no matches found

      @throws RuntimeException if:
              - User not found
              - Invalid date format
              - Invalid filter parameters

     * @see FinancialRecordService#getFilteredRecords(String, String, String, String, String)
     */
    @GetMapping("/filter")
    public ResponseEntity<?> getFilteredRecords(
            @RequestParam(required = false) String type,
            @RequestParam(required = false) String category,
            @RequestParam(required = false) String startDate,
            @RequestParam(required = false) String endDate,
            Principal principal) {
        String email = principal.getName();
        List<FinancialRecordResponseDTO> records = recordService.getFilteredRecords(email, type, category, startDate, endDate);

        return (records.isEmpty())
                ? ResponseEntity.status(HttpStatus.OK).body("No records found matching the filter criteria.")
                : ResponseEntity.ok(records);
    }

    /*
      Retrieves all financial records for a specific user.
      Endpoint: GET /records/user/{userId}

      Access Control:
      - VIEWER: Cannot access (403 Forbidden)
      - ANALYST: Can view any user's records (read-only)
      - ADMIN: Can view any user's records (full access)

      Use Cases:
      - ANALYST reviewing specific employee's spending
      - ADMIN auditing a user's transaction history
      - Generating user-specific reports

      @param userId The ID of the user whose records to retrieve
      @param principal Spring Security principal containing logged-in user's email

      @return List of FinancialRecordResponseDTO for the specified user

      @throws RuntimeException if:
              - Current user is VIEWER
              - Target user doesn't exist
              - User not found
      @see FinancialRecordService#getRecordsByUserId(Long, String)
     */
    @GetMapping("/user/{userId}")
    public ResponseEntity<?> getRecordsByUserId(@PathVariable Long userId, Principal principal) {
        String email = principal.getName();
        List<FinancialRecordResponseDTO> records = recordService.getRecordsByUserId(userId, email);

        return (records.isEmpty())
                ? ResponseEntity.status(HttpStatus.OK).body("No records found for user ID: " + userId)
                : ResponseEntity.ok(records);
    }

    /*
      Updates an existing financial record.
      Endpoint: PUT /records/update/{id}

      This endpoint allows ADMIN users to modify existing transactions.
      Partial updates are supported (only provided fields are updated).

      Security:
      - Only ADMIN role can update records
      - Cannot update non-existent records

      Updatable Fields:
      - amount: Transaction amount
      - type: INCOME or EXPENSE
      - category: Category name
      - date: Transaction date
      - description: Transaction notes

      @param id The unique identifier of the record to update
      @param dto FinancialRecordRequestDTO containing updated fields
      @param principal Spring Security principal containing logged-in user's email

      @return FinancialRecordResponseDTO containing updated record details

      @throws RuntimeException if:
              - Current user is not ADMIN
              - Record not found
              - Invalid update data
     * @see FinancialRecordService#updateRecord(Long, FinancialRecordRequestDTO, String)
     */
    @PutMapping("/update/{id}")
    public ResponseEntity<?> updateRecord(
            @PathVariable Long id,
            @RequestBody FinancialRecordRequestDTO dto,
            Principal principal) {
        FinancialRecordResponseDTO updatedRecord = recordService.updateRecord(id, dto, principal.getName());
        return ResponseEntity.ok(updatedRecord);
    }

    /*
      Retrieves paginated financial records for efficient data loading.
      Endpoint: GET /records/paginated?page={page}&size={size}

      This endpoint implements pagination to improve performance when dealing with large datasets. Useful for:
      - Infinite scrolling
      - "Load more" functionality
      - Reducing initial load time

      Role-Based Data:
      - VIEWER: Returns their own records (paginated)
      - ANALYST: Returns all users' records (paginated)
      - ADMIN: Returns all users' records (paginated)

      Pagination Parameters:
      - page: Page number (0-indexed, default: 0)
      - size: Number of records per page (default: 5, max: 100)

      Response Headers:
      - X-Total-Count: Total number of records
      - X-Total-Pages: Total number of pages
      - X-Current-Page: Current page number

      @param page Page number (0-indexed, defaults to 0)
      @param size Number of records per page (defaults to 5)
      @param principal Spring Security principal containing logged-in user's email
      @return List of FinancialRecordResponseDTO for the requested page
      @throws RuntimeException if user not found
      @see FinancialRecordService#getRecordsPaginated(String, int, int)
     */
    @GetMapping("/paginated")
    public ResponseEntity<?> getPaginatedRecords(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "5") int size,
            Principal principal) {
        List<FinancialRecordResponseDTO> records = recordService.getRecordsPaginated(principal.getName(), page, size);

        return (records.isEmpty())
                ? ResponseEntity.status(HttpStatus.OK).body("No records found on page " + page)
                : ResponseEntity.ok(records);
    }
}
