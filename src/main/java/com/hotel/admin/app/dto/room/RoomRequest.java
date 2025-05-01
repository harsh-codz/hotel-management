package com.hotel.admin.app.dto.room;

 // Assuming RoomStatus enum exists
import jakarta.validation.constraints.*;
import lombok.Getter;
import lombok.Setter;

import java.math.BigDecimal;
import java.util.Set;

import com.hotel.admin.app.entity.enums.RoomStatus;

@Getter @Setter
public class RoomRequest {

    // Not included for create, required for update context but usually not updatable
    // private Long id;

    @NotBlank(message = "Room number is mandatory")
    @Size(max = 10, message = "Room number cannot exceed 10 characters")
    private String roomNumber; // Non-editable after creation logic in service

    @NotNull(message = "Room type ID is mandatory")
    private Long roomTypeId;

    @NotNull(message = "Price per night is mandatory")
    @Positive(message = "Price per night must be positive")
    @Digits(integer=10, fraction=2, message = "Price format invalid")
    private BigDecimal pricePerNight;

    @NotNull(message = "Room status is mandatory")
    private RoomStatus roomStatus; // Enum: AVAILABLE, OCCUPIED, UNDER_MAINTENANCE

    @NotNull(message = "Maximum occupancy is mandatory")
    @Positive(message = "Maximum occupancy must be positive")
    @Min(value = 1, message = "Maximum occupancy must be at least 1")
    private Integer maxOccupancy;

    @Size(max = 500, message = "Description cannot exceed 500 characters")
    private String description;

    private Set<Long> amenityIds; // Can be null or empty
}