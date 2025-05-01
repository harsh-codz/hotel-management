package com.hotel.admin.app.dto.complaint;


import com.hotel.admin.app.dto.user.UserBasicInfoResponse; // Reusing user DTO
import lombok.*;
import java.time.LocalDateTime;

@Getter @Setter
public class ComplaintActionResponse {
    private Long id;
    private UserBasicInfoResponse user; // Who took the action
    private LocalDateTime timestamp;
    private String actionDescription;
    private String internalNotes;
}