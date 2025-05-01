package com.hotel.admin.app.dto.user;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter @Setter @NoArgsConstructor @AllArgsConstructor
public class UserBasicInfoResponse {
    private Long id;
    private String username;
    private String fullName;
    private String email;
}