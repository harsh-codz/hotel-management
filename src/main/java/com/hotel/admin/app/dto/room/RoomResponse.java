package com.hotel.admin.app.dto.room;

// Assuming RoomStatus enum exists
import lombok.Getter;
import lombok.Setter;

import java.math.BigDecimal;
import java.util.Set;

import com.hotel.admin.app.entity.enums.RoomStatus;

@Getter @Setter
public class RoomResponse {
    private Long id;
    private String roomNumber;
    private RoomTypeResponse roomType; // Nested DTO
    private BigDecimal pricePerNight;
    private RoomStatus roomStatus;
    private Integer maxOccupancy;
    private String description;
    private Set<AmenityResponse> amenities; // Nested DTOs
}