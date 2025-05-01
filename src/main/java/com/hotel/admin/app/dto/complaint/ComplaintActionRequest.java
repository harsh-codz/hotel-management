package com.hotel.admin.app.dto.complaint;


import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.*;

@Getter @Setter
public class ComplaintActionRequest {
    @NotBlank(message = "Action description is mandatory")
    @Size(max = 1000, message = "Action description cannot exceed 1000 characters")
    private String actionDescription;

    @Size(max = 1000, message = "Internal notes cannot exceed 1000 characters")
    private String internalNotes;
    // user and timestamp are set by the service
}