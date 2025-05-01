package com.hotel.admin.app.dto.complaint;


import com.hotel.admin.app.dto.user.UserBasicInfoResponse;
import com.hotel.admin.app.entity.enums.ComplaintPriority;
import com.hotel.admin.app.entity.enums.ComplaintStatus;

import lombok.*;
import java.time.LocalDateTime;
import java.util.List;

@Getter @Setter
public class ComplaintResponse {
    private Long id;
    private String complaintId;
    private UserBasicInfoResponse user; // Customer who submitted
    private LocalDateTime submissionDate;
    private ComplaintCategoryResponse category; // Or String categoryName
    private String description;
    private ComplaintStatus status;
    private UserBasicInfoResponse assignedStaff; // Staff assigned (can be null)
    private ComplaintPriority priorityLevel;
    private List<ComplaintActionResponse> actions; // History of actions
}