package com.hotel.admin.app.dto.user;


import jakarta.validation.constraints.*;
import lombok.*;
import java.util.Set;

@Getter @Setter
public class UserUpdateRequest {
    // Username usually not changeable
    @NotBlank(message = "Email is mandatory")
    @Email(message = "Invalid email format")
    @Size(max = 100)
    private String email;

    @NotEmpty(message = "User must have at least one role")
    private Set<String> roles; // Allow updating roles

    @Size(max = 100)
    private String fullName;

    @Size(max = 20)
    private String contactPhone;
    // Status updated via separate activate/deactivate endpoints
}
