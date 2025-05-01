package com.hotel.admin.app.service;


import com.hotel.admin.app.dto.auth.UserInfoResponse; // Reusing DTO
import com.hotel.admin.app.dto.profile.ChangePasswordRequest;
import com.hotel.admin.app.dto.user.RoleResponse;
import com.hotel.admin.app.dto.user.UserCreateRequest;
import com.hotel.admin.app.dto.user.UserResponse;
import com.hotel.admin.app.dto.user.UserUpdateRequest;
import com.hotel.admin.app.entity.Role;
import com.hotel.admin.app.entity.Role.ERole;
import com.hotel.admin.app.entity.User;
import com.hotel.admin.app.entity.enums.UserStatus;
// Optional DTO
import com.hotel.admin.app.exception.BadRequestException;
import com.hotel.admin.app.exception.ConflictException;
import com.hotel.admin.app.exception.ResourceNotFoundException;
import com.hotel.admin.app.exception.UnauthorizedException;
import com.hotel.admin.app.repository.RoleRepository;
import com.hotel.admin.app.repository.UserRepository; // Repository
import com.hotel.admin.app.security.services.UserDetailsImpl; // Security Principal
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.crypto.password.PasswordEncoder; // Inject PasswordEncoder
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.security.SecureRandom;
import java.util.Base64;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;


@Service
@RequiredArgsConstructor
public class UserService { // Assuming this service exists or is created

    private static final Logger log = LoggerFactory.getLogger(UserService.class);

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder; // Inject PasswordEncoder
    private final BillService.UserInfoService userInfoService; // Reuse helper
   private final RoleRepository roleRepository; 

    // --- Profile Management Methods ---

    // Get current user's profile
    @Transactional(readOnly = true)
    public UserInfoResponse getCurrentUserProfile() {
        User currentUser = getCurrentAuthenticatedUserEntity();
        return mapToUserInfoResponse(currentUser); // Use a consistent mapping method
    }


    // Change current user's password
    @Transactional
    public void changeCurrentUserPassword(ChangePasswordRequest request) {
        User currentUser = getCurrentAuthenticatedUserEntity();
        log.info("Attempting password change for user: {}", currentUser.getUsername());

        // 1. Validate current password
        if (!passwordEncoder.matches(request.getCurrentPassword(), currentUser.getPassword())) {
            throw new BadRequestException("Incorrect current password.");
        }

        // 2. Check if new password is same as old (optional, good practice)
         if (passwordEncoder.matches(request.getNewPassword(), currentUser.getPassword())) {
            throw new BadRequestException("New password cannot be the same as the current password.");
        }


        // 3. Optional: Validate new password confirmation (if DTO included it)
        // if (!request.getNewPassword().equals(request.getConfirmPassword())) {
        //     throw new BadRequestException("New passwords do not match.");
        // }

        // 4. Encode and set new password
        currentUser.setPassword(passwordEncoder.encode(request.getNewPassword()));

        // 5. Important: Reset the password reset flag if it was set
        currentUser.setPasswordResetRequired(false);

        userRepository.save(currentUser);
        log.info("Password successfully changed for user: {}", currentUser.getUsername());
    }

  

    // Gets the currently authenticated User *ENTITY*
    private User getCurrentAuthenticatedUserEntity() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication == null || !authentication.isAuthenticated() || !(authentication.getPrincipal() instanceof UserDetailsImpl)) {
            throw new UnauthorizedException("User is not authenticated.");
        }
        UserDetailsImpl userDetails = (UserDetailsImpl) authentication.getPrincipal();
        // Fetch the full User entity from repository using the ID or username
        return userRepository.findById(userDetails.getId()) // Assuming UserDetailsImpl has getId()
               .orElseThrow(() -> new ResourceNotFoundException("User", "id", userDetails.getId())); // Should not happen if authenticated
    }

    // Consistent mapping to UserInfoResponse
     private UserInfoResponse mapToUserInfoResponse(User user) {
        if (user == null) return null;
        return new UserInfoResponse(
                user.getId(),
                user.getUsername(),
                user.getEmail(),
                user.getRoles().stream()
                .map(role -> role.getName().name()) // Get the ERole enum, then call .name() for its String value
                .collect(Collectors.toList()),
                user.isPasswordResetRequired() // Assuming boolean field or getter exists
        );
    }


  @Transactional
    public UserResponse createUser(UserCreateRequest request) {
        log.info("Attempting to create user with username: {}", request.getUsername());

        if (userRepository.existsByUsername(request.getUsername())) {
            throw new ConflictException("Username '" + request.getUsername() + "' is already taken.");
        }
        if (userRepository.existsByEmail(request.getEmail())) {
            throw new ConflictException("Email '" + request.getEmail() + "' is already in use.");
        }

        // Find roles by name
        Set<ERole> requestedERoles = request.getRoles().stream()
                                            .map(this::mapStringToERole) // Convert string to ERole enum
                                            .collect(Collectors.toSet());
        Set<Role> roles = roleRepository.findByNameIn(requestedERoles);
        if (roles.size() != requestedERoles.size()) {
             throw new BadRequestException("One or more provided roles do not exist.");
        }


        User user = new User();
        user.setUsername(request.getUsername());
        user.setEmail(request.getEmail());
        user.setFullName(request.getFullName());
        user.setContactPhone(request.getContactPhone());
        user.setRoles(roles);
        user.setStatus(UserStatus.ACTIVE); // Start as active

        // Generate, hash, and set password
        String generatedPassword = generateRandomPassword();
        user.setPassword(passwordEncoder.encode(generatedPassword));
        user.setPasswordResetRequired(true); // Require reset on first login

        User savedUser = userRepository.save(user);
        log.info("User {} created successfully. Initial password needs reset.", savedUser.getUsername());
        // Note: We generally DO NOT return the generated password in the response.
        // It should be communicated securely or handled by the initial login flow.
        return mapToUserResponse(savedUser);
    }










 @Transactional(readOnly = true)
    public UserResponse getUserById(Long id) {
        User user = findUserByIdOrFail(id); // Reuse existing helper or create one
        return mapToUserResponse(user);
    }

    // List/Search Users (Admin)
    @Transactional(readOnly = true)
    public Page<UserResponse> findUsers(Specification<User> spec, Pageable pageable) {
        Page<User> userPage = userRepository.findAll(spec, pageable);
        return userPage.map(this::mapToUserResponse);
    }























  @Transactional
    public UserResponse updateUser(Long id, UserUpdateRequest request) {
        User existingUser = findUserByIdOrFail(id);
        log.info("Attempting to update user: {}", existingUser.getUsername());

        // Validate email uniqueness if changed
         if (!existingUser.getEmail().equals(request.getEmail()) && userRepository.existsByEmailAndIdNot(request.getEmail(), id)) {
             throw new ConflictException("Email address '" + request.getEmail() + "' is already in use by another account.");
         }
         existingUser.setEmail(request.getEmail());
         existingUser.setFullName(request.getFullName());
         existingUser.setContactPhone(request.getContactPhone());

         // Update Roles
        Set<ERole> requestedERoles = request.getRoles().stream()
                                            .map(this::mapStringToERole)
                                            .collect(Collectors.toSet());
        Set<Role> newRoles = roleRepository.findByNameIn(requestedERoles);
        if (newRoles.size() != requestedERoles.size()) {
            throw new BadRequestException("One or more provided roles do not exist.");
        }
        existingUser.setRoles(newRoles);


        User updatedUser = userRepository.save(existingUser);
        log.info("User {} updated successfully.", updatedUser.getUsername());
        return mapToUserResponse(updatedUser);
    }












    @Transactional
    public void resetUserPassword(Long id) {
        User user = findUserByIdOrFail(id);
         log.info("Attempting to reset password for user: {}", user.getUsername());

        String generatedPassword = generateRandomPassword();
        user.setPassword(passwordEncoder.encode(generatedPassword));
        user.setPasswordResetRequired(true); // Force reset on next login

        userRepository.save(user);
        log.info("Password reset for user {}. User must reset on next login.", user.getUsername());
        // TODO: How to communicate the new temporary password? Email? Secure channel?
        // For now, just log it for testing (REMOVE IN PRODUCTION)
        log.warn("TEMP PASSWORD for {}: {}", user.getUsername(), generatedPassword);
    }








    @Transactional
    public void deactivateUser(Long id) {
        User user = findUserByIdOrFail(id);
         log.info("Deactivating user: {}", user.getUsername());
         if(user.getStatus() == UserStatus.INACTIVE) {
            log.warn("User {} is already inactive.", user.getUsername());
            return; // Or throw exception?
         }
        // TODO: Add checks? Cannot deactivate self? Cannot deactivate last admin?
        user.setStatus(UserStatus.INACTIVE);
        userRepository.save(user);
        log.info("User {} deactivated.", user.getUsername());
    }








  @Transactional
    public void activateUser(Long id) {
        User user = findUserByIdOrFail(id);
        log.info("Activating user: {}", user.getUsername());
         if(user.getStatus() == UserStatus.ACTIVE) {
            log.warn("User {} is already active.", user.getUsername());
            return; // Or throw exception?
         }
        user.setStatus(UserStatus.ACTIVE);
        // Optional: Clear password reset flag if activating? Depends on policy.
        // user.setPasswordResetRequired(false);
        userRepository.save(user);
        log.info("User {} activated.", user.getUsername());
    }







   @Transactional(readOnly = true)
    public List<RoleResponse> getAllRoles() {
        return roleRepository.findAll().stream()
                .map(role -> new RoleResponse(role.getId(), role.getName().name())) // Map ERole to String
                .collect(Collectors.toList());
    }

    private User findUserByIdOrFail(Long id) {
        return userRepository.findById(id)
               .orElseThrow(() -> new ResourceNotFoundException("User", "id", id));
    }




private String generateRandomPassword() {
        // Simple example, consider more robust generation (e.g., Apache Commons Lang RandomStringUtils)
        SecureRandom random = new SecureRandom();
        byte[] bytes = new byte[12]; // 12 bytes = 16 Base64 chars
        random.nextBytes(bytes);
        return Base64.getUrlEncoder().withoutPadding().encodeToString(bytes);
    }

     // Convert Role String (e.g., "ROLE_ADMIN") to ERole enum
     private ERole mapStringToERole(String roleName) {
        try {
            return ERole.valueOf(roleName.toUpperCase());
        } catch (IllegalArgumentException e) {
            throw new BadRequestException("Invalid role name provided: " + roleName);
        }
    }




       private UserResponse mapToUserResponse(User user) {
         if (user == null) return null;
         UserResponse dto = new UserResponse();
         dto.setId(user.getId());
         dto.setUsername(user.getUsername());
         dto.setEmail(user.getEmail());
         dto.setFullName(user.getFullName());
         dto.setContactPhone(user.getContactPhone());
         dto.setStatus(user.getStatus());
         dto.setPasswordResetRequired(user.isPasswordResetRequired());
         dto.setRoles(user.getRoles().stream()
                         .map(role -> role.getName().name()) // Map ERole to String name
                         .collect(Collectors.toSet()));
         return dto;
    }


    public Page<UserResponse> findUsersByCriteria(
        String username, String email, UserStatus status, Set<String> roles,
        Pageable pageable) {

    // This method now needs to call a repository method capable of handling dynamic criteria
    Page<User> userPage = userRepository.findWithDynamicQuery( // Or a better name
            username, email, status, roles, pageable
    );

    return userPage.map(this::mapToUserResponse); // Reuse existing mapping logic
}

  

   

}
