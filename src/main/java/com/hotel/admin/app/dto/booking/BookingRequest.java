package com.hotel.admin.app.dto.booking;

import jakarta.validation.constraints.*;
import lombok.Getter;
import lombok.Setter;

import java.math.BigDecimal;
import java.time.LocalDate;

@Getter @Setter
public class BookingRequest {

    @NotNull(message = "User ID (Customer) is mandatory")
    private Long userId;

    @NotNull(message = "Room ID is mandatory")
    private Long roomId;

    @NotNull(message = "Check-in date is mandatory")
    @FutureOrPresent(message = "Check-in date must be today or in the future")
    private LocalDate checkInDate;

    @NotNull(message = "Check-out date is mandatory")
    @Future(message = "Check-out date must be in the future")
    private LocalDate checkOutDate;

    @NotNull(message = "Number of guests is mandatory")
    @Min(value = 1, message = "Number of guests must be at least 1")
    private Integer numberOfGuests;

    @Size(max = 500, message = "Special requests cannot exceed 500 characters")
    private String specialRequests;

    @Size(max = 50, message = "Payment method cannot exceed 50 characters")
    private String paymentMethod; // Consider enum later if needed

    @PositiveOrZero(message = "Deposit amount cannot be negative")
    private BigDecimal depositAmount;

    // Note: status is set by the service, totalAmount is calculated by the service
    // Note: bookingDate is set by the service

    // Custom validation for checkOutDate > checkInDate can be added via @AssertTrue or a custom validator
}