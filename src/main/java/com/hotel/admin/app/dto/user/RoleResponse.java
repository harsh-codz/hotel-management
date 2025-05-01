package com.hotel.admin.app.dto.user;

import lombok.*;

@Getter @Setter @NoArgsConstructor @AllArgsConstructor
public class RoleResponse {
    private Long id;
    private String name; // e.g., "ROLE_ADMIN"
}