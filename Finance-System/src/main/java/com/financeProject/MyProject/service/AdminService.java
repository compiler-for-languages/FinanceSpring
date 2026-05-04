package com.financeProject.MyProject.service;

import com.financeProject.MyProject.dto.UserRequestDTO;
import com.financeProject.MyProject.dto.UserResponseDTO;
import com.financeProject.MyProject.model.Role;
import com.financeProject.MyProject.model.User;
import com.financeProject.MyProject.repository.RoleRepository;
import com.financeProject.MyProject.repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;
import com.financeProject.MyProject.repository.FinancialRecordRepository;
import java.util.Optional;

/*
      Service layer for handling administrative business logic.
      This service encapsulates all user management operations including:
          - User creation with role validation
          - User retrieval with DTO conversion
          - User status management
          - User role management
      All methods enforce ADMIN-only access and include comprehensive validation rules to maintain system integrity.
 */

@Service
@Transactional
public class AdminService {

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private RoleRepository roleRepository;

    @Autowired
    private PasswordEncoder passwordEncoder;

    @Autowired
    private FinancialRecordRepository recordRepository;

    /*
          Creates a new user in the system with specified role.

          Business Rules:
           1. Only ADMIN users can create new users
           2. Cannot create ADMIN role users (security restriction)
           3. Email must be unique across the system
           4. Password is encoded before storage
           5. New users are created with ACTIVE status by default
          @param dto UserRequestDTO containing:
                      - name: User's full name (required)
                      - email: Unique email address (required)
                      - password: Plain text password (required)
                      - role: VIEWER or ANALYST (required)
          @param currentUserEmail Email of the ADMIN creating the user
          @return UserResponseDTO containing created user's details without password
          @throws RuntimeException if:
                  - Current user doesn't exist in database
                  - Current user doesn't have ADMIN role
                  - Email already exists in system
                  - Role is invalid or ADMIN
          @see UserRequestDTO
          @see UserResponseDTO
     */
    public UserResponseDTO createUser(UserRequestDTO dto, String currentUserEmail) {

        User currentUser = userRepository.findByEmail(currentUserEmail)
                .orElseThrow(() -> new RuntimeException("User not found"));

        // Allowing only admin
        if (!currentUser.getRole().getName().equals("ADMIN")) {
            throw new RuntimeException("Only ADMIN can create users");
        }

        // Block ADMIN Creation
        if ("ADMIN".equalsIgnoreCase(dto.getRole())) {
            throw new RuntimeException("Admin creation is not allowed");
        }


        // Check if email already exists
        Optional<User> existing = userRepository.findByEmail(dto.getEmail());
        if (existing.isPresent()) {
            throw new RuntimeException("Email already exists");
        }

        // Fetch role from DB (VIEWER / ANALYST / ADMIN)
        Role role = roleRepository.findByName(dto.getRole())
                .orElseThrow(() -> new RuntimeException("Invalid role"));
        if ("ADMIN".equalsIgnoreCase(dto.getRole())) {
            throw new RuntimeException("Admin creation is not allowed");
        } // To block creating any other admin

        // Convert DTO → Entity
        User user = new User();
        user.setName(dto.getName());
        user.setEmail(dto.getEmail());
        user.setPassword(passwordEncoder.encode(dto.getPassword()));// (later can hash)
        user.setRole(role);
        user.setStatus("ACTIVE");

        // Save user
        User savedUser = userRepository.save(user);

        // Convert Entity → Response DTO
        return convertToDTO(savedUser);
    }

    /*
        Retrieves all users from the system.
        This method returns all registered users regardless of role or status.
        No filtering is applied - use with caution in production environments with large user bases (consider adding pagination).
        @return List of UserResponseDTO containing all users' details, returns empty list if no users exist
        @note No ADMIN check is performed here as it's handled at controller level via Spring Security @PreAuthorize annotation
     */
    public List<UserResponseDTO> getAllUsers() {

        return userRepository.findAll()
                .stream()
                .map(this::convertToDTO)
                .toList();
    }

    /*
        Retrieves a specific user by their unique identifier.
        @param id The unique identifier of the user to retrieve
        @return UserResponseDTO containing user's details
        @throws RuntimeException if user with given ID is not found
        @note Throws exception with message "User not found" for invalid IDs
     */
    public UserResponseDTO getUser(Long id) {

        User user = userRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("User not found"));

        return convertToDTO(user);
    }


    /*
        Updates the status of a user (ACTIVE/INACTIVE).
        Business Rules:
             1. Only ADMIN can update user status
             2. ADMIN users cannot be deactivated (system integrity)
             3. Status must be either "ACTIVE" or "INACTIVE"
             4. Inactive users cannot log in or access system
        Use Cases:
             - Temporarily suspend a user account (set INACTIVE)
             - Reinstate a suspended account (set ACTIVE)
             - Disable accounts of terminated employees
        @param userId ID of the user whose status is being updated
        @param status New status value: "ACTIVE" or "INACTIVE"
        @param adminEmail Email of the ADMIN performing this operation
        @return UserResponseDTO with updated status
        @throws RuntimeException if:
             - ADMIN user doesn't exist
             - Caller doesn't have ADMIN role
             - Target user doesn't exist
             - Target user has ADMIN role (protected)
             - Invalid status value provided
     */
    public UserResponseDTO updateUserStatus(Long userId, String status, String adminEmail) {

        // Get current user (who is making request)
        User admin = userRepository.findByEmail(adminEmail)
                .orElseThrow(() -> new RuntimeException("Admin not found"));

        // Ensure only ADMIN can update
        if (!admin.getRole().getName().equals("ADMIN")) {
            throw new RuntimeException("Only ADMIN can update user status");
        }

        // Get target user
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new RuntimeException("User not found"));

        // Optional: prevent deactivating ADMIN
        if (user.getRole().getName().equals("ADMIN")) {
            throw new RuntimeException("Cannot modify ADMIN status");
        }

        // Validate status input
        if (!status.equals("ACTIVE") && !status.equals("INACTIVE")) {
            throw new RuntimeException("Invalid status");
        }

        // Update
        user.setStatus(status);

        User updated = userRepository.save(user);

        return convertToDTO(updated);
    }

    /*
          Updates the role of a user (VIEWER/ANALYST).
          Business Rules:
          1. Only ADMIN can change user roles
          2. ADMIN role cannot be modified or assigned
          3. Target user cannot be an ADMIN
          4. Role must exist in database
          Use Cases:
          - Promote a VIEWER to ANALYST (grant read-all access)
          - Demote an ANALYST to VIEWER (restrict to personal data only)
          - Adjust permissions based on job function changes
          Role Capabilities:
          - VIEWER: Can only see their own data
          - ANALYST: Can see all data (read-only)
          - ADMIN: Full system control (cannot be assigned via this method)

          @param userId ID of the user whose role is being updated
          @param roleName New role name: "VIEWER" or "ANALYST"
          @param adminEmail Email of the ADMIN performing this operation

          @return UserResponseDTO with updated role

          @throws RuntimeException if:
                  - ADMIN user doesn't exist
                  - Caller doesn't have ADMIN role
                  - Target user doesn't exist
                  - Target user has ADMIN role (protected)
                  - Attempting to assign ADMIN role
                  - Role name doesn't exist in database
     */
    public UserResponseDTO updateUserRole(Long userId, String roleName, String adminEmail) {

        //  Get current user (who is making request)
        User admin = userRepository.findByEmail(adminEmail)
                .orElseThrow(() -> new RuntimeException("Admin not found"));

        //  Only ADMIN allowed
        if (!admin.getRole().getName().equals("ADMIN")) {
            throw new RuntimeException("Only ADMIN can update roles");
        }

        // Get target user
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new RuntimeException("User not found"));

        // Prevent modifying ADMIN user
        if (user.getRole().getName().equals("ADMIN")) {
            throw new RuntimeException("Cannot modify ADMIN user");
        }

        // Prevent assigning ADMIN role
        if ("ADMIN".equalsIgnoreCase(roleName)) {
            throw new RuntimeException("Admin creation is not allowed");
        }

        // Get role from DB
        Role role = roleRepository.findByName(roleName)
                .orElseThrow(() -> new RuntimeException("Role not found"));

        // Update role
        user.setRole(role);

        User updated = userRepository.save(user);

        return convertToDTO(updated);
    }

    public UserResponseDTO updateUser(Long userId, UserRequestDTO dto, String adminEmail) {
        User admin = userRepository.findByEmail(adminEmail)
                .orElseThrow(() -> new RuntimeException("Admin not found"));

        if (!admin.getRole().getName().equals("ADMIN")) {
            throw new RuntimeException("Only ADMIN can update users");
        }

        User user = userRepository.findById(userId)
                .orElseThrow(() -> new RuntimeException("User not found"));

        if (user.getRole().getName().equals("ADMIN")) {
            throw new RuntimeException("Cannot modify ADMIN user");
        }

        if (dto.getName() != null && !dto.getName().isEmpty()) {
            user.setName(dto.getName());
        }

        if (dto.getRole() != null && !dto.getRole().isEmpty()) {
            if ("ADMIN".equalsIgnoreCase(dto.getRole())) {
                throw new RuntimeException("Admin creation is not allowed");
            }
            Role role = roleRepository.findByName(dto.getRole())
                    .orElseThrow(() -> new RuntimeException("Role not found"));
            user.setRole(role);
        }

        if (dto.getPassword() != null && !dto.getPassword().isEmpty()) {
            user.setPassword(passwordEncoder.encode(dto.getPassword()));
        }

        User updated = userRepository.save(user);
        return convertToDTO(updated);
    }

    public void deleteUser(Long userId, String adminEmail) {
        User admin = userRepository.findByEmail(adminEmail)
                .orElseThrow(() -> new RuntimeException("Admin not found"));

        if (!admin.getRole().getName().equals("ADMIN")) {
            throw new RuntimeException("Only ADMIN can delete users");
        }

        User user = userRepository.findById(userId)
                .orElseThrow(() -> new RuntimeException("User not found"));

        if (user.getRole().getName().equals("ADMIN")) {
            throw new RuntimeException("Cannot delete ADMIN user");
        }

        // Delete all financial records associated with the user
        recordRepository.deleteAll(recordRepository.findByUserId(userId));
        userRepository.delete(user);
    }

    /*
          Converts a User entity to a UserResponseDTO.

          This helper method ensures consistent data transformation across all service methods.
          It excludes sensitive fields like password.
          @param user The User entity to convert (must not be null)
          @return UserResponseDTO containing:
                  - id: Unique identifier
                  - name: User's full name
                  - email: User's email address
                  - role: Role name (VIEWER/ANALYST/ADMIN)
                  - status: Account status (ACTIVE/INACTIVE)

          @throws NullPointerException if user parameter is null
          @see UserResponseDTO
     */
    private UserResponseDTO convertToDTO(User user) {

        UserResponseDTO dto = new UserResponseDTO();

        dto.setId(user.getId());
        dto.setName(user.getName());
        dto.setEmail(user.getEmail());

        // Important: Role is entity → we return role name
        dto.setRole(user.getRole().getName());

        dto.setStatus(user.getStatus());

        return dto;
    }


}
