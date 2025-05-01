package com.hotel.admin.app.controller;


import com.hotel.admin.app.dto.complaint.ComplaintActionRequest;
import com.hotel.admin.app.dto.complaint.ComplaintResponse;
import com.hotel.admin.app.dto.complaint.ComplaintStatusUpdateRequest;
import com.hotel.admin.app.entity.Complaint; // For Specification
import com.hotel.admin.app.service.ComplaintService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
// Imports for specification-arg-resolver (optional)

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification; // Use Spring's Specification
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/staff/complaints") // Staff endpoint base path
@RequiredArgsConstructor
@PreAuthorize("hasRole('STAFF')") // Secure all staff complaint endpoints
public class StaffComplaintController {

    private final ComplaintService complaintService;

    // GET /api/staff/complaints (List/Search Assigned - Staff)
    @GetMapping
    public ResponseEntity<Page<ComplaintResponse>> findAssignedComplaints(
            // Add @Spec annotations here if using specification-arg-resolver
            // The service layer will automatically add the assignedStaffId filter
            Specification<Complaint> spec, // Placeholder if using library
            Pageable pageable) {
         // TODO: Implement Specification building based on request params if not using library
        Page<ComplaintResponse> complaints = complaintService.findComplaintsForCurrentStaff(spec, pageable);
        return ResponseEntity.ok(complaints);
    }

    // GET /api/staff/complaints/{id} (View Assigned - Staff)
    @GetMapping("/{id}")
    public ResponseEntity<ComplaintResponse> getAssignedComplaintById(@PathVariable Long id) {
        // Service layer handles the check to ensure it's assigned to current staff
        return ResponseEntity.ok(complaintService.getComplaintByIdForStaff(id));
    }

    // PUT /api/staff/complaints/{id}/status (Update Status - Staff)
    @PutMapping("/{id}/status")
    public ResponseEntity<ComplaintResponse> updateAssignedComplaintStatus(
            @PathVariable Long id,
            @Valid @RequestBody ComplaintStatusUpdateRequest statusRequest) {
        // Service layer handles the check to ensure it's assigned
        ComplaintResponse updatedComplaint = complaintService.updateComplaintStatusByStaff(id, statusRequest.getStatus());
        return ResponseEntity.ok(updatedComplaint);
    }

    // POST /api/staff/complaints/{id}/actions (Add Action Log - Staff)
    @PostMapping("/{id}/actions")
    public ResponseEntity<ComplaintResponse> addAssignedComplaintAction(
            @PathVariable Long id,
            @Valid @RequestBody ComplaintActionRequest actionRequest) {
        // Service layer handles the check to ensure it's assigned
        ComplaintResponse updatedComplaint = complaintService.addActionByStaff(id, actionRequest);
        return ResponseEntity.ok(updatedComplaint); // Return updated complaint with new action
    }
}