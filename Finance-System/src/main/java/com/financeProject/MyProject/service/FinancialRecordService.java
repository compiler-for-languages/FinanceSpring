package com.financeProject.MyProject.service;

import com.financeProject.MyProject.dto.FinancialRecordRequestDTO;
import com.financeProject.MyProject.dto.FinancialRecordResponseDTO;
import com.financeProject.MyProject.model.FinancialRecord;
import com.financeProject.MyProject.model.User;
import com.financeProject.MyProject.repository.FinancialRecordRepository;
import com.financeProject.MyProject.repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.util.List;
import java.util.stream.Collectors;

/*
    Service layer for managing financial record operations.
    Purpose:
        - Handles all CRUD operations for financial transactions
        - Implements role-based access control for record management
        - Provides filtering, pagination, and soft delete functionality

        Soft Delete Strategy:
      - Records are never permanently deleted from database
      - Deleted flag is set to true instead of removing record
      - All queries filter by deleted=false for active records
      - Benefits: Data recovery, audit trails, historical reporting
 */

@Service
public class FinancialRecordService {

    @Autowired
    private FinancialRecordRepository recordRepository;

    @Autowired
    private UserRepository userRepository;

    /*
      Creates a new financial record for a target user.
      Access: ADMIN only

      Validation Steps:
      - Verifies the requesting user has ADMIN role
      - Validates that target user exists in database
      - Converts DTO to entity and saves to database

      @param adminEmail Email of the ADMIN creating the record
      @param targetUserId ID of the user for whom record is being created
      @param dto FinancialRecordRequestDTO containing transaction details
      @return FinancialRecordResponseDTO with created record details
      @throws RuntimeException if admin not found, target user not found, or not ADMIN
     */
    public FinancialRecordResponseDTO createRecord(String adminEmail,
                                                   Long targetUserId,
                                                   FinancialRecordRequestDTO dto) {

        //  who is making request
        User admin = userRepository.findByEmail(adminEmail)
                .orElseThrow(() -> new RuntimeException("User not found"));

        // only ADMIN allowed
        if (!admin.getRole().getName().equals("ADMIN")) {
            throw new RuntimeException("Only ADMIN can create records");
        }

        // target user
        User targetUser = userRepository.findById(targetUserId)
                .orElseThrow(() -> new RuntimeException("Target user not found"));

        if (targetUser.getRole().getName().equals("ADMIN")) {
            throw new RuntimeException("Cannot create record for admin");
        }

        FinancialRecord record = new FinancialRecord();

        record.setUser(targetUser);
        record.setAmount(dto.getAmount());
        record.setType(dto.getType());
        record.setCategory(dto.getCategory());
        record.setRecordDate(dto.getRecordDate());
        record.setNotes(dto.getNotes());

        return convertToDTO(recordRepository.save(record));
    }

    /*
      Retrieves financial records based on user role.

      Access:
      - VIEWER: Returns only their own records
      - ANALYST: Returns all users' records
      - ADMIN: Returns all users' records

      Soft Delete Filtering:
      - Only returns records where deleted = false
      - Deleted records are excluded from all queries

      @param email - Email of the authenticated user
      @return List of FinancialRecordResponseDTO for authorized records
      @throws RuntimeException if user not found
     */

    public List<FinancialRecordResponseDTO> getRecords(String email) {

        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new RuntimeException("User not found"));

        String role = user.getRole().getName();

        List<FinancialRecord> records;

        System.out.println("EMAIL: " + email);
        System.out.println("ROLE: " + role);
        System.out.println("USER ID: " + user.getId());

        if (role.equals("VIEWER")) {
            // Only own records
//            records = recordRepository.findByUserId(user.getId());
              records = recordRepository.findByUserIdAndDeletedFalse(user.getId());
        } else {
            // ANALYST / ADMIN → all records
//            records = recordRepository.findAll();
              records = recordRepository.findByDeletedFalse();
        }

        return records.stream()
                .map(this::convertToDTO)
                .toList();
    }

    /*
      Retrieves a specific financial record by its ID.

     Access Control:
     - VIEWER: Can only access their own records
     - ANALYST: Can access any record
     - ADMIN: Can access any record

     @param recordId ID of the record to retrieve
     @param email Email of the authenticated user
     @return FinancialRecordResponseDTO containing record details
     @throws RuntimeException if user not found, record not found, or access denied
     */

    public FinancialRecordResponseDTO getRecordById(Long recordId, String email) {

        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new RuntimeException("User not found"));

        FinancialRecord record = recordRepository.findById(recordId)
                .orElseThrow(() -> new RuntimeException("Record not found"));

        String role = user.getRole().getName();

        //  VIEWER restriction
        if (role.equals("VIEWER") && !record.getUser().getId().equals(user.getId())) {
            throw new RuntimeException("Access denied");
        }

        return convertToDTO(record);
    }

    /*
      Deletes all financial records using soft delete.

      Access: ADMIN only

      Soft Delete Implementation:
      - Finds all active records (deleted = false)
      - Sets deleted flag to true for each record
      - Saves all updated records back to database
      - Original data remains for audit purposes

      @param email Email of the authenticated user
      @throws RuntimeException if user not found or not ADMIN
     */

    public void deleteAllRecords(String email) {

        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new RuntimeException("User not found"));

        // Only ADMIN allowed
        if (!user.getRole().getName().equals("ADMIN")) {
            throw new RuntimeException("Only ADMIN can delete all records");
        }

//        recordRepository.deleteAll();

        List<FinancialRecord> records = recordRepository.findByDeletedFalse();

        for (FinancialRecord r : records) {
            r.setDeleted(true);
        }

        recordRepository.saveAll(records);
    }

    /*
      Deletes a specific financial record by ID using soft delete.

      Access: ADMIN only

      Soft Delete Implementation:
      - Finds the record by ID
      - Sets deleted flag to true instead of removing from database
      - Preserves data for audit and potential recovery

      @param recordId ID of the record to delete
      @param email Email of the authenticated user
      @throws RuntimeException if user not found, not ADMIN, or record not found
     */

    public void deleteRecordById(Long recordId, String email) {

        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new RuntimeException("User not found"));

        // Only ADMIN allowed
        if (!user.getRole().getName().equals("ADMIN")) {
            throw new RuntimeException("Only ADMIN can delete records");
        }

        FinancialRecord record = recordRepository.findById(recordId)
                .orElseThrow(() -> new RuntimeException("Record not found"));

//        recordRepository.delete(record);

        record.setDeleted(true);
        recordRepository.save(record);
    }

    /*
      Retrieves filtered financial records based on criteria.

      Available Filters:
      - type: INCOME or EXPENSE
      - category: Category name (e.g., groceries, rent)
      - startDate and endDate: Date range for filtering

      Access:
       - VIEWER: Filters only their own records
       - ANALYST/ADMIN: Filters all users' records

      @param email - Email of the authenticated user
      @param type  Optional filter by transaction type
      @param category Optional  filter by category
      @param startDate Optional start date for date range
      @param endDate Optional end date for date range
      @return List of filtered FinancialRecordResponseDTO
      @throws RuntimeException if user not found
     */

    public List<FinancialRecordResponseDTO> getFilteredRecords(
            String email,
            String type,
            String category,
            String startDate,
            String endDate) {

        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new RuntimeException("User not found"));

        String role = user.getRole().getName();

        List<FinancialRecord> records;

        //  ROLE-BASED FETCH (MAIN FIX)
        if (role.equals("VIEWER")) {
//            records = recordRepository.findByUserId(user.getId()); // Only own
            records = recordRepository.findByUserIdAndDeletedFalse(user.getId());
        } else {
            records = recordRepository.findByDeletedFalse(); // ANALYST & ADMIN
        }

        // Apply filters (same as before)

        if (type != null && !type.isEmpty()) {
            records = records.stream()
                    .filter(r -> r.getType().equalsIgnoreCase(type))
                    .toList();
        }

        if (category != null && !category.isEmpty()) {
            records = records.stream()
                    .filter(r -> r.getCategory().toLowerCase().contains(category.toLowerCase()))
                    .toList();
        }

        if (startDate != null && !startDate.isEmpty() && endDate != null && !endDate.isEmpty()) {
            LocalDate start = LocalDate.parse(startDate);
            LocalDate end = LocalDate.parse(endDate);

            records = records.stream()
                    .filter(r -> !r.getRecordDate().isBefore(start) &&
                            !r.getRecordDate().isAfter(end))
                    .toList();
        }

        return records.stream()
                .map(this::convertToDTO)
                .toList();
    }

    /*
      Retrieves all financial records for a specific user by their ID.

      Access Control:
      - VIEWER: Can only access their own records (userId must match)
      - ANALYST: Can access any user's records
      - ADMIN: Can access any user's records

      @param userId ID of the user whose records to retrieve
      @param email Email of the authenticated user
      @return List of FinancialRecordResponseDTO for the specified user
      @throws RuntimeException if user not found or access denied
     */
    public List<FinancialRecordResponseDTO> getRecordsByUserId(Long userId, String email) {

        User currentUser = userRepository.findByEmail(email).orElseThrow();

        String role = currentUser.getRole().getName();

        // VIEWER → only own data
        if (role.equals("VIEWER") && !currentUser.getId().equals(userId)) {
            throw new RuntimeException("Access denied");
        }

        List<FinancialRecord> records = recordRepository.findByUserIdAndDeletedFalse(userId);

        return records.stream()
                .map(this::convertToDTO)
                .toList();
    }

    /*
      Updates an existing financial record.

      Access: ADMIN only

      Updatable Fields:
      - amount: Transaction amount
      - type: INCOME or EXPENSE
      - category: Category name
      - recordDate: Transaction date
      - notes: Additional description

      @param id - ID of the record to update
      @param dto FinancialRecordRequestDTO containing updated values
      @param email -  Email of the authenticated user
      @return FinancialRecordResponseDTO with updated record details
      @throws RuntimeException if user not found, not ADMIN, or record not found
     */
    public FinancialRecordResponseDTO updateRecord(Long id,
                                                   FinancialRecordRequestDTO dto,
                                                   String email) {

        User user = userRepository.findByEmail(email).orElseThrow();

        if (!user.getRole().getName().equals("ADMIN")) {
            throw new RuntimeException("Only ADMIN can update records");
        }

        FinancialRecord record = recordRepository.findById(id).orElseThrow();

        if (dto.getUserId() != null) {
            User targetUser = userRepository.findById(dto.getUserId())
                    .orElseThrow(() -> new RuntimeException("Target user not found"));

            if (targetUser.getRole().getName().equals("ADMIN")) {
                throw new RuntimeException("Cannot create record for admin");
            }

            record.setUser(targetUser);
        }

        record.setAmount(dto.getAmount());
        record.setType(dto.getType());
        record.setCategory(dto.getCategory());
        record.setRecordDate(dto.getRecordDate());
        record.setNotes(dto.getNotes());

        return convertToDTO(recordRepository.save(record));
    }

    /*
      Retrieves paginated financial records for efficient data loading.

      Access:
      - VIEWER: Returns paginated own records
      - ANALYST/ADMIN: Returns paginated all records

      Pagination Benefits:
      - Reduces initial load time for large datasets
      - Enables infinite scrolling or "load more" features
      - Prevents memory issues with thousands of records

      @param email - Email of the authenticated user
      @param page - Page number (0-indexed)
      @param size Number of records per page
      @return List of FinancialRecordResponseDTO for the requested page
      @throws RuntimeException if user not found
     */
    public List<FinancialRecordResponseDTO> getRecordsPaginated(
            String email, int page, int size) {

        User user = userRepository.findByEmail(email).orElseThrow();

        String role = user.getRole().getName();

        Pageable pageable = PageRequest.of(page, size);

        Page<FinancialRecord> recordsPage;

        if (role.equals("VIEWER")) {
            recordsPage = recordRepository
                    .findByUserIdAndDeletedFalse(user.getId(), pageable);
        } else {
            recordsPage = recordRepository
                    .findByDeletedFalse(pageable);
        }

        return recordsPage.getContent()
                .stream()
                .map(this::convertToDTO)
                .toList();
    }



    //  HELPER METHOD (Entity → DTO conversion)
    private FinancialRecordResponseDTO convertToDTO(FinancialRecord record) {

        FinancialRecordResponseDTO dto = new FinancialRecordResponseDTO();

        dto.setId(record.getId());
        dto.setAmount(record.getAmount());
        dto.setType(record.getType());
        dto.setCategory(record.getCategory());
        dto.setRecordDate(record.getRecordDate());
        dto.setUserId(record.getUser().getId());
        dto.setUserName(record.getUser().getName());
        dto.setDescription(record.getNotes());

        return dto;
    }
}

