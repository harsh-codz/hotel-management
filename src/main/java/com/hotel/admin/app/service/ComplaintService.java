package com.hotel.admin.app.service;


import com.hotel.admin.app.dto.complaint.*;

import com.hotel.admin.app.exception.*;
import com.hotel.admin.app.entity.*; // Entities
import com.hotel.admin.app.entity.enums.ComplaintPriority;
import com.hotel.admin.app.entity.enums.ComplaintStatus;
import com.hotel.admin.app.repository.*; // Repositories
import com.hotel.admin.app.security.services.UserDetailsImpl; // To get current user
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;


import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class ComplaintService {

    private static final Logger log = LoggerFactory.getLogger(ComplaintService.class);

    private final ComplaintRepository complaintRepository;
    private final UserRepository userRepository;
    private final ComplaintCategoryRepository categoryRepository; // If using category entity
    private final BillService.UserInfoService userInfoService; // Reusing user mapping helper

    // --- Admin Operations (US022) ---

    // List/Search All Complaints (Admin View)
    public Page<ComplaintResponse> findComplaintsForAdmin(Specification<Complaint> spec, Pageable pageable) {
        Page<Complaint> complaintPage = complaintRepository.findAll(spec, pageable);
        return complaintPage.map(this::mapToComplaintResponse);
    }

    // Get Single Complaint (Admin View)
    public ComplaintResponse getComplaintByIdForAdmin(Long id) {
        Complaint complaint = findComplaintByIdOrFail(id);
        return mapToComplaintResponse(complaint);
    }

    // Create Complaint (Admin possibly creates on behalf of user)
    @Transactional
    public ComplaintResponse createComplaint(ComplaintRequest request) {
        log.info("Admin creating complaint for user ID: {}", request.getUserId());
        User user = findUserByIdOrFail(request.getUserId());
        // Optional: Validate user role is CUSTOMER?

        ComplaintCategory category = findCategoryByIdOrFail(request.getCategoryId()); // If using entity

        Complaint complaint = new Complaint();
        complaint.setUser(user);
        complaint.setCategory(category); // If using entity
        // complaint.setCategory(request.getCategoryName()); // If using String
        complaint.setDescription(request.getDescription());
        complaint.setSubmissionDate(LocalDateTime.now());
        complaint.setStatus(ComplaintStatus.OPEN); // Default status
        complaint.setPriorityLevel(request.getPriorityLevel() != null ? request.getPriorityLevel() : ComplaintPriority.MEDIUM); // Default priority
        complaint.setComplaintId(generateComplaintId());

        Complaint savedComplaint = complaintRepository.save(complaint);
        log.info("Complaint ID {} created successfully.", savedComplaint.getId());
        return mapToComplaintResponse(savedComplaint);
    }

    // Assign Complaint to Staff (Admin Only)
    @Transactional
    public ComplaintResponse assignComplaint(Long complaintId, Long staffUserId) {
        log.info("Admin assigning complaint ID {} to staff ID {}", complaintId, staffUserId);
        Complaint complaint = findComplaintByIdOrFail(complaintId);
        User staffUser = findUserByIdOrFail(staffUserId);

        // Optional: Validate staffUser has ROLE_STAFF
        boolean isStaff = staffUser.getRoles().stream().anyMatch(role -> role.getName().equals("ROLE_STAFF"));
        if (!isStaff) {
            throw new BadRequestException("User ID " + staffUserId + " does not have the STAFF role.");
        }

        complaint.setAssignedStaff(staffUser);
        Complaint updatedComplaint = complaintRepository.save(complaint);

        // Log this assignment as an action? Optional.
        // addAction(complaintId, new ComplaintActionRequest("Complaint assigned to " + staffUser.getUsername(), null));

        log.info("Complaint ID {} assigned to staff {}", complaintId, staffUser.getUsername());
        return mapToComplaintResponse(updatedComplaint);
    }

    // Update Complaint Status (Admin can update any)
    @Transactional
    public ComplaintResponse updateComplaintStatusByAdmin(Long complaintId, ComplaintStatus newStatus) {
         log.info("Admin updating status for complaint ID {} to {}", complaintId, newStatus);
        Complaint complaint = findComplaintByIdOrFail(complaintId);
        // Add any status transition validation logic if needed
        complaint.setStatus(newStatus);
        Complaint updatedComplaint = complaintRepository.save(complaint);
        log.info("Status updated for complaint ID {}", complaintId);

         // Log status change? Optional.
        // addAction(complaintId, new ComplaintActionRequest("Status changed to " + newStatus, null));

        return mapToComplaintResponse(updatedComplaint);
    }

    // Add Action Log (Admin can add to any)
    @Transactional
    public ComplaintResponse addActionByAdmin(Long complaintId, ComplaintActionRequest actionRequest) {
         log.info("Admin adding action to complaint ID {}", complaintId);
        Complaint complaint = findComplaintByIdOrFail(complaintId);
        User currentUser = getCurrentAuthenticatedUser(); // Get logged-in Admin user

        ComplaintAction action = new ComplaintAction();
        action.setComplaint(complaint);
        action.setUser(currentUser);
        action.setTimestamp(LocalDateTime.now());
        action.setActionDescription(actionRequest.getActionDescription());
        action.setInternalNotes(actionRequest.getInternalNotes());

        // Persist the action - cascading should handle it if configured, else save explicitly
        // complaintActionRepository.save(action); // Explicit save if needed
        complaint.addAction(action); // Add to list if bidirectional

        complaintRepository.save(complaint); // Save complaint to persist cascaded action

        log.info("Action added to complaint ID {}", complaintId);
        // Return the updated complaint which now includes the new action
        return mapToComplaintResponse(complaint);
    }


    // --- Staff Operations (US023) ---

    // List Complaints Assigned to Current Staff Member
    public Page<ComplaintResponse> findComplaintsForCurrentStaff(Specification<Complaint> spec, Pageable pageable) {
        User currentUser = getCurrentAuthenticatedUser();
        log.debug("Fetching complaints assigned to staff ID: {}", currentUser.getId());

        // Combine the external specification (filters) with the staff assignment constraint
        Specification<Complaint> assignedSpec = (root, query, cb) ->
                cb.equal(root.get("assignedStaff").get("id"), currentUser.getId());

        Specification<Complaint> finalSpec = spec == null ? assignedSpec : spec.and(assignedSpec);

        Page<Complaint> complaintPage = complaintRepository.findAll(finalSpec, pageable);
        return complaintPage.map(this::mapToComplaintResponse);
    }

     // Get Single Complaint (Staff View - Must be assigned)
     public ComplaintResponse getComplaintByIdForStaff(Long id) {
         User currentUser = getCurrentAuthenticatedUser();
         Complaint complaint = findComplaintByIdOrFail(id);
         // --- Authorization Check ---
         if (complaint.getAssignedStaff() == null || !complaint.getAssignedStaff().getId().equals(currentUser.getId())) {
             log.warn("Staff user {} attempted to access unassigned complaint ID {}", currentUser.getUsername(), id);
             throw new ForbiddenAccessException("You are not assigned to this complaint.");
         }
         return mapToComplaintResponse(complaint);
     }

    // Update Complaint Status (Staff can update assigned complaints)
    @Transactional
    public ComplaintResponse updateComplaintStatusByStaff(Long complaintId, ComplaintStatus newStatus) {
        User currentUser = getCurrentAuthenticatedUser();
        Complaint complaint = findComplaintByIdOrFail(complaintId);
        log.info("Staff {} attempting to update status for complaint ID {} to {}", currentUser.getUsername(), complaintId, newStatus);

        // --- Authorization Check ---
         if (complaint.getAssignedStaff() == null || !complaint.getAssignedStaff().getId().equals(currentUser.getId())) {
            log.warn("Staff user {} attempted to update status of unassigned complaint ID {}", currentUser.getUsername(), complaintId);
            throw new ForbiddenAccessException("You are not assigned to this complaint.");
        }

         // Add any status transition validation logic if needed
         complaint.setStatus(newStatus);
         Complaint updatedComplaint = complaintRepository.save(complaint);
         log.info("Status updated for complaint ID {} by staff {}", complaintId, currentUser.getUsername());

         // Log status change? Optional.
         // addAction(complaintId, new ComplaintActionRequest("Status changed to " + newStatus, null));

         return mapToComplaintResponse(updatedComplaint);
    }


    // Add Action Log (Staff can add to assigned complaints)
    @Transactional
    public ComplaintResponse addActionByStaff(Long complaintId, ComplaintActionRequest actionRequest) {
        User currentUser = getCurrentAuthenticatedUser();
        Complaint complaint = findComplaintByIdOrFail(complaintId);
         log.info("Staff {} attempting to add action to complaint ID {}", currentUser.getUsername(), complaintId);

         // --- Authorization Check ---
         if (complaint.getAssignedStaff() == null || !complaint.getAssignedStaff().getId().equals(currentUser.getId())) {
            log.warn("Staff user {} attempted to add action to unassigned complaint ID {}", currentUser.getUsername(), complaintId);
            throw new ForbiddenAccessException("You are not assigned to this complaint.");
        }

        ComplaintAction action = new ComplaintAction();
        action.setComplaint(complaint);
        action.setUser(currentUser); // Action logged by the current staff user
        action.setTimestamp(LocalDateTime.now());
        action.setActionDescription(actionRequest.getActionDescription());
        action.setInternalNotes(actionRequest.getInternalNotes());

        complaint.addAction(action); // Add to list if bidirectional
        complaintRepository.save(complaint); // Persist cascaded action

        log.info("Action added to complaint ID {} by staff {}", complaintId, currentUser.getUsername());
        return mapToComplaintResponse(complaint);
    }

    // --- Lookup data ---
    public List<ComplaintCategoryResponse> getAllCategories() {
        return categoryRepository.findAll().stream()
                .map(c -> new ComplaintCategoryResponse(c.getId(), c.getName()))
                .collect(Collectors.toList());
    }

    // --- Helper Methods ---

    private Complaint findComplaintByIdOrFail(Long id) {
        return complaintRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Complaint", "id", id));
    }

    private User findUserByIdOrFail(Long id) {
         return userRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("User", "id", id));
    }

     private ComplaintCategory findCategoryByIdOrFail(Long id) {
        return categoryRepository.findById(id)
                .orElseThrow(() -> new BadRequestException("Invalid Complaint Category ID: " + id));
    }

     private User getCurrentAuthenticatedUser() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication == null || !authentication.isAuthenticated() || !(authentication.getPrincipal() instanceof UserDetailsImpl)) {
            // This shouldn't happen for secured endpoints, but good defensive check
            throw new UnauthorizedException("User is not authenticated.");
        }
        UserDetailsImpl userDetails = (UserDetailsImpl) authentication.getPrincipal();
        // Need to fetch the full User entity from the repository using the ID or username from UserDetailsImpl
        return findUserByIdOrFail(userDetails.getId()); // Assuming getId() exists in UserDetailsImpl
    }

    private String generateComplaintId() {
        return "COMP-" + UUID.randomUUID().toString().substring(0, 8).toUpperCase();
    }


    // --- Mapping ---

     private ComplaintResponse mapToComplaintResponse(Complaint complaint) {
        ComplaintResponse response = new ComplaintResponse();
        response.setId(complaint.getId());
        response.setComplaintId(complaint.getComplaintId());
        response.setSubmissionDate(complaint.getSubmissionDate());
        response.setDescription(complaint.getDescription());
        response.setStatus(complaint.getStatus());
        response.setPriorityLevel(complaint.getPriorityLevel());

        if (complaint.getUser() != null) {
            response.setUser(userInfoService.mapToUserBasicInfoResponse(complaint.getUser()));
        }
        if (complaint.getCategory() != null) { // If using category entity
             response.setCategory(new ComplaintCategoryResponse(complaint.getCategory().getId(), complaint.getCategory().getName()));
        }
         // else { response.setCategoryName(complaint.getCategory()); } // If using String category

        if (complaint.getAssignedStaff() != null) {
            response.setAssignedStaff(userInfoService.mapToUserBasicInfoResponse(complaint.getAssignedStaff()));
        }

        if (complaint.getActions() != null) {
            // Ensure actions are loaded (may require join fetch in query or transactional context)
            response.setActions(complaint.getActions().stream()
                    .map(this::mapToActionResponse)
                    .collect(Collectors.toList()));
        }

        return response;
    }

    private ComplaintActionResponse mapToActionResponse(ComplaintAction action) {
        ComplaintActionResponse response = new ComplaintActionResponse();
        response.setId(action.getId());
        response.setTimestamp(action.getTimestamp());
        response.setActionDescription(action.getActionDescription());
        response.setInternalNotes(action.getInternalNotes());
        if (action.getUser() != null) {
            response.setUser(userInfoService.mapToUserBasicInfoResponse(action.getUser()));
        }
        return response;
    }
}
