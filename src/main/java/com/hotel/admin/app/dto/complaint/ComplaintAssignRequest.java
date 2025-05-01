package com.hotel.admin.app.dto.complaint;

import jakarta.validation.constraints.NotNull;
import lombok.*;
@Getter @Setter
public class ComplaintAssignRequest {
    @NotNull(message = "Staff User ID is mandatory for assignment")
    private Long assignedStaffId;
}