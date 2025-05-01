package com.hotel.admin.app.controller;


import com.hotel.admin.app.dto.user.*; // Import user DTOs
import com.hotel.admin.app.entity.enums.UserStatus;
// Removed Specification import
// Import enum for filtering
import com.hotel.admin.app.service.UserService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
// Removed Specification related imports
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Set; // For filtering by roles

@RestController
@RequestMapping("/api/admin/users")
@RequiredArgsConstructor
@PreAuthorize("hasRole('ADMIN')")
public class UserController {

    private final UserService userService;

    // GET /api/admin/users/roles - Get available roles (Remains the same)
    @GetMapping("/roles")
    public ResponseEntity<List<RoleResponse>> getAvailableRoles() {
        return ResponseEntity.ok(userService.getAllRoles());
    }

    // POST /api/admin/users - Create User (Remains the same)
    @PostMapping
    public ResponseEntity<UserResponse> createUser(@Valid @RequestBody UserCreateRequest userCreateRequest) {
        UserResponse createdUser = userService.createUser(userCreateRequest);
        return ResponseEntity.status(HttpStatus.CREATED).body(createdUser);
    }

    // GET /api/admin/users - List/Search Users without Specification
    @GetMapping
    public ResponseEntity<Page<UserResponse>> findUsers(
            // --- Receive individual filter parameters ---
            @RequestParam(required = false) String username,
            @RequestParam(required = false) String email,
            @RequestParam(required = false) UserStatus status,
            @RequestParam(required = false) Set<String> roles, // e.g., ?roles=ROLE_ADMIN,ROLE_STAFF
            // --- End filter parameters ---
            Pageable pageable // Spring handles pagination/sorting params automatically
    ) {
        // Pass parameters down to the service layer
        Page<UserResponse> users = userService.findUsersByCriteria(
                username, email, status, roles, pageable
        );
        return ResponseEntity.ok(users);
    }

    // GET /api/admin/users/{id} - Get User by ID (Remains the same)
    @GetMapping("/{id}")
    public ResponseEntity<UserResponse> getUserById(@PathVariable Long id) {
        return ResponseEntity.ok(userService.getUserById(id));
    }

    // PUT /api/admin/users/{id} - Update User (Remains the same)
    @PutMapping("/{id}")
    public ResponseEntity<UserResponse> updateUser(@PathVariable Long id, @Valid @RequestBody UserUpdateRequest userUpdateRequest) {
        UserResponse updatedUser = userService.updateUser(id, userUpdateRequest);
        return ResponseEntity.ok(updatedUser);
    }

    // PUT /api/admin/users/{id}/reset-password - Reset Password (Remains the same)
    @PutMapping("/{id}/reset-password")
    public ResponseEntity<Void> resetPassword(@PathVariable Long id) {
        userService.resetUserPassword(id);
        return ResponseEntity.ok().build();
    }

    // PUT /api/admin/users/{id}/deactivate - Deactivate User (Remains the same)
    @PutMapping("/{id}/deactivate")
    public ResponseEntity<Void> deactivateUser(@PathVariable Long id) {
        userService.deactivateUser(id);
        return ResponseEntity.noContent().build();
    }

    // PUT /api/admin/users/{id}/activate - Activate User (Remains the same)
    @PutMapping("/{id}/activate")
    public ResponseEntity<Void> activateUser(@PathVariable Long id) {
        userService.activateUser(id);
        return ResponseEntity.noContent().build();
    }
}