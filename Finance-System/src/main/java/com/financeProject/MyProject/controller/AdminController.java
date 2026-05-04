package com.financeProject.MyProject.controller;

import com.financeProject.MyProject.dto.UserRequestDTO;
import com.financeProject.MyProject.dto.UserResponseDTO;
import com.financeProject.MyProject.service.AdminService;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/*
        Controller responsible for handling all ADMIN-specific operations
        Base URL: /admin
        This controller exposes endpoints that are restricted to ADMIN role
        such as managing users ( create, update, fetch, delete..)
 */

@RestController
@RequestMapping("/admin")
public class AdminController {


    @Autowired
    private AdminService adminService;

    /*
        Creates a new user in the system.
        This endpoint allows ADMIN users to create new VIEWER or ANALYST accounts.
        ADMIN accounts cannot be created through this endpoint for security reasons.
        Endpoint: POST /admin/users/add
        @param dto UserRequestDTO containing user details:
                - name: Full name of the user
                - email: Unique email address (used as username)
                - password: Plain text password (will be encoded)
                - role: Either "VIEWER" or "ANALYST" (ADMIN not allowed)
        @param principal Spring Security principal containing current logged-in user's email
        @return ResponseEntity containing either:
          - List of UserResponseDTO if users exist
          - String message "No users found. Table is empty." if no users
        @throws RuntimeException if:
               - Caller is not ADMIN
               - Email already exists in system
               - Role is invalid or "ADMIN"
               - Any validation fails
     */
    @PostMapping("/users/add")
    public ResponseEntity<?> createUser(@RequestBody UserRequestDTO dto,
                                      java.security.Principal principal) {

        String email = principal.getName();
        UserResponseDTO createdUser = adminService.createUser(dto, email);
        return ResponseEntity.status(HttpStatus.CREATED).body(createdUser);
    }


    /*
        Retrieves a list of all registered users in the system.
        This endpoint returns all users regardless of their role or status.
        Accessible only to ADMIN role.
        Endpoint: GET /admin/users/all
        @return List of UserResponseDTO containing all users' details(id, name, email, role, status)
        @throws RuntimeException if caller doesn't have ADMIN authority (handled by security layer)
     */
    @GetMapping("/users/all")
    public ResponseEntity<?> getAllUsers() {
        List<UserResponseDTO> users = adminService.getAllUsers();
        return(users.isEmpty()) ? ResponseEntity.status(HttpStatus.OK)
                .body("No users found.Table is empty") : ResponseEntity.ok(users);
    }

    /*
        Retrieves a specific user by their unique ID.
        Endpoint: GET /admin/users/{id}
        @param id The unique identifier of the user to retrieve
        @return UserResponseDTO containing the user's details
        @throws RuntimeException if user with given ID doesn't exist
        @example GET /admin/users/5
     */
    @GetMapping("/users/{id}")
    public ResponseEntity<?> getUserById(@PathVariable Long id) {
        UserResponseDTO user = adminService.getUser(id);
        return ResponseEntity.ok(user);
    }

    /*
        Updates the status of a user (ACTIVE/INACTIVE).
        This endpoint allows ADMIN to activate or deactivate user accounts.
        Inactive users cannot log in or access any system resources.
        ADMIN users cannot be deactivated through this endpoint.
        Endpoint: PATCH /admin/users/{id}/status
        @param id - The unique identifier of the user to update
        @param status - New status value: "ACTIVE" or "INACTIVE"
        @param principal - Spring Security principal containing current ADMIN's email
        @return UserResponseDTO with updated status information
        @throws RuntimeException if:
                  - Caller is not ADMIN
                  - Target user doesn't exist
                  - Target user has ADMIN role
                  - Invalid status value provided
        @example Request: PATCH /admin/users/7/status?status=INACTIVE
     */
    @PatchMapping("/users/{id}/status")
    public ResponseEntity<?> updateUserStatus(@PathVariable Long id,
                                              @RequestParam String status,
                                              java.security.Principal principal) {
        String adminEmail = principal.getName();
        UserResponseDTO updatedUser = adminService.updateUserStatus(id, status, adminEmail);
        return ResponseEntity.ok(updatedUser);
    }

    /*
        Updates the role of a user (VIEWER/ANALYST).
        This endpoint allows ADMIN to change user permissions by assigning different roles.
        ADMIN role cannot be assigned to any user through this endpoint for security purposes.
        Endpoint: PATCH /admin/users/{id}/role
        @param id The unique identifier of the user to update
        @param role New role value: "VIEWER" or "ANALYST" (ADMIN not allowed)
        @param principal Spring Security principal containing current ADMIN's email
        @return UserResponseDTO with updated role information
        @throws RuntimeException if:
                 - Caller is not ADMIN
                 - Target user doesn't exist
                 - Target user has ADMIN role (cannot modify ADMIN)
                 - Attempting to assign ADMIN role
                 - Invalid role name provided
        @example Request: PATCH /admin/users/7/role?role=VIEWER

     */
    @PatchMapping("/users/{id}/role")
    public ResponseEntity<?> updateUserRole(@PathVariable Long id,
                                            @RequestParam String role,
                                            java.security.Principal principal) {
        String adminEmail = principal.getName();
        UserResponseDTO updatedUser = adminService.updateUserRole(id, role, adminEmail);
        return ResponseEntity.ok(updatedUser);
    }

    @PutMapping("/users/{id}")
    public ResponseEntity<?> updateUser(@PathVariable Long id,
                                      @RequestBody UserRequestDTO dto,
                                      java.security.Principal principal) {
        String adminEmail = principal.getName();
        UserResponseDTO updatedUser = adminService.updateUser(id, dto, adminEmail);
        return ResponseEntity.ok(updatedUser);
    }

    @DeleteMapping("/users/{id}")
    public ResponseEntity<?> deleteUser(@PathVariable Long id,
                                      java.security.Principal principal) {
        String adminEmail = principal.getName();
        adminService.deleteUser(id, adminEmail);
        return ResponseEntity.ok("User deleted successfully");
    }
}