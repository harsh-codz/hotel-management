package com.hotel.admin.app.controller;


import com.hotel.admin.app.dto.auth.UserInfoResponse;
import com.hotel.admin.app.dto.profile.ChangePasswordRequest;

import com.hotel.admin.app.service.UserService; // Assuming profile methods are in UserService
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

@RestController
// Define a base path for profile operations - could be /api/profile or grouped under /api/admin|staff
@RequestMapping("/api/profile") // Or /api/me or /api/account
@RequiredArgsConstructor
// Allow any authenticated user (Admin or Staff) to access their own profile
@PreAuthorize("isAuthenticated()") // Or specific roles like "hasAnyRole('ADMIN', 'STAFF')"
public class ProfileController {

    private final UserService userService; // Assuming profile methods are here

    // GET /api/profile/me - Get current user's profile
    @GetMapping("/me")
    public ResponseEntity<UserInfoResponse> getCurrentUserProfile() {
        return ResponseEntity.ok(userService.getCurrentUserProfile());
    }

    // PUT /api/profile/change-password - Change current user's password
    @PutMapping("/change-password")
    public ResponseEntity<Void> changePassword(@Valid @RequestBody ChangePasswordRequest changePasswordRequest) {
        userService.changeCurrentUserPassword(changePasswordRequest);
        return ResponseEntity.ok().build(); // Return 200 OK on success, no body needed
    }

    // Optional: PUT /api/profile/me - Update current user's profile details
   

}
