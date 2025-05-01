package com.hotel.admin.app.dto.complaint;

import com.hotel.admin.app.entity.enums.ComplaintStatus;

import jakarta.validation.constraints.NotNull;
import lombok.*;
@Getter @Setter
public class ComplaintStatusUpdateRequest {
    @NotNull(message = "New status is mandatory")
    private ComplaintStatus status;
}
