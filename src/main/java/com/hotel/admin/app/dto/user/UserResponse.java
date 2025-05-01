package com.hotel.admin.app.dto.user;



import lombok.*;
import java.util.Set;

import com.hotel.admin.app.entity.enums.UserStatus;

@Getter @Setter
public class UserResponse {
    private Long id;
    private String username;
    private String email;
    private String fullName;
    private String contactPhone;
    private UserStatus status;
    private Set<String> roles; // Role names
    private boolean passwordResetRequired;
}