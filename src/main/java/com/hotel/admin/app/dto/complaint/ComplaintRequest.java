package com.hotel.admin.app.dto.complaint;



import com.hotel.admin.app.entity.enums.ComplaintPriority;

import jakarta.validation.constraints.*;
import lombok.*;

@Getter @Setter
public class ComplaintRequest {
    @NotNull(message = "User ID (Customer) is mandatory")
    private Long userId;

    @NotNull(message = "Category ID is mandatory") // If using category entity
    private Long categoryId;
    // OR: @NotBlank if using String category in entity

    @NotBlank(message = "Description is mandatory")
    @Size(max = 2000) // Define a reasonable max length
    private String description;

    // Status, assignedStaff, submissionDate, priority are set by service initially or via updates
    private ComplaintPriority priorityLevel; // Optional on creation
}