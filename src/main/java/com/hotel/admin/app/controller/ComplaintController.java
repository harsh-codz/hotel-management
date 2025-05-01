package com.hotel.admin.app.controller;


import com.hotel.admin.app.dto.complaint.*;
import com.hotel.admin.app.entity.Complaint; // For Specification
import com.hotel.admin.app.service.ComplaintService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
// Imports for specification-arg-resolver (optional)
// import net.kaczmarzyk.spring.data.jpa.domain.*;
// import net.kaczmarzyk.spring.data.jpa.web.annotation.*;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification; // Use Spring's Specification
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import java.util.List;

@RestController
@RequestMapping("/api/admin/complaints") // Admin endpoint base path
@RequiredArgsConstructor
@PreAuthorize("hasRole('ADMIN')") // Secure all admin complaint endpoints
public class ComplaintController {

    private final ComplaintService complaintService;

    // GET /api/admin/complaint-categories (Helper)
    @GetMapping("/categories")
    public ResponseEntity<List<ComplaintCategoryResponse>> getComplaintCategories() {
        return ResponseEntity.ok(complaintService.getAllCategories());
    }

    // GET /api/admin/complaints (List/Search All - Admin)
    @GetMapping
    public ResponseEntity<Page<ComplaintResponse>> findComplaints(
            // Add @Spec annotations here if using specification-arg-resolver
            // Or manually build Specification based on @RequestParam
            Specification<Complaint> spec, // Placeholder if using library
            Pageable pageable) {
        // TODO: Implement Specification building based on request params if not using library
        Page<ComplaintResponse> complaints = complaintService.findComplaintsForAdmin(spec, pageable);
        return ResponseEntity.ok(complaints);
    }

    // GET /api/admin/complaints/{id} (View Any - Admin)
    @GetMapping("/{id}")
    public ResponseEntity<ComplaintResponse> getComplaintById(@PathVariable Long id) {
        return ResponseEntity.ok(complaintService.getComplaintByIdForAdmin(id));
    }

    // POST /api/admin/complaints (Create - Admin)
    @PostMapping
    public ResponseEntity<ComplaintResponse> createComplaint(@Valid @RequestBody ComplaintRequest complaintRequest) {
        ComplaintResponse createdComplaint = complaintService.createComplaint(complaintRequest);
        return ResponseEntity.status(HttpStatus.CREATED).body(createdComplaint);
    }

    // PUT /api/admin/complaints/{id}/assign (Assign - Admin)
    @PutMapping("/{id}/assign")
    public ResponseEntity<ComplaintResponse> assignComplaint(
            @PathVariable Long id,
            @Valid @RequestBody ComplaintAssignRequest assignRequest) {
        ComplaintResponse updatedComplaint = complaintService.assignComplaint(id, assignRequest.getAssignedStaffId());
        return ResponseEntity.ok(updatedComplaint);
    }

    // PUT /api/admin/complaints/{id}/status (Update Status - Admin)
    @PutMapping("/{id}/status")
    public ResponseEntity<ComplaintResponse> updateComplaintStatus(
            @PathVariable Long id,
            @Valid @RequestBody ComplaintStatusUpdateRequest statusRequest) {
        ComplaintResponse updatedComplaint = complaintService.updateComplaintStatusByAdmin(id, statusRequest.getStatus());
        return ResponseEntity.ok(updatedComplaint);
    }

    // POST /api/admin/complaints/{id}/actions (Add Action Log - Admin)
    @PostMapping("/{id}/actions")
    public ResponseEntity<ComplaintResponse> addComplaintAction(
            @PathVariable Long id,
            @Valid @RequestBody ComplaintActionRequest actionRequest) {
        ComplaintResponse updatedComplaint = complaintService.addActionByAdmin(id, actionRequest);
        return ResponseEntity.ok(updatedComplaint); // Return updated complaint with new action
    }
}