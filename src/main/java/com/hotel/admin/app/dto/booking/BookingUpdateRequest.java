package com.hotel.admin.app.dto.booking;

import jakarta.validation.constraints.*;
import lombok.Getter;
import lombok.Setter;

import java.math.BigDecimal;
import java.time.LocalDate;

// Could potentially inherit from BookingRequest or be separate
@Getter @Setter
public class BookingUpdateRequest {

    // Usually not updatable, or requires careful validation
    // private Long userId;
    // private Long roomId;

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
    private String paymentMethod;

    @PositiveOrZero(message = "Deposit amount cannot be negative")
    private BigDecimal depositAmount;

    // Status might be updated via dedicated endpoints (e.g., check-in, check-out, cancel)
    // @NotNull // Or handle status changes separately
    // private BookingStatus status;
}