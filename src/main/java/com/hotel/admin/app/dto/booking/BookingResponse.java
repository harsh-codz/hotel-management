package com.hotel.admin.app.dto.booking;

import com.hotel.admin.app.dto.room.RoomResponse; // Reuse Room DTO
import com.hotel.admin.app.dto.user.UserBasicInfoResponse; // Need a basic User DTO
import com.hotel.admin.app.entity.enums.BookingStatus;
import lombok.Getter;
import lombok.Setter;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;

@Getter @Setter
public class BookingResponse {
    private Long id;
    private String bookingId; // Or use id if preferred
    private UserBasicInfoResponse user; // Basic user info
    private RoomResponse room; // Full room info
    private LocalDate checkInDate;
    private LocalDate checkOutDate;
    private Integer numberOfGuests;
    private BookingStatus status;
    private LocalDateTime bookingDate;
    private BigDecimal totalAmount;
    private String specialRequests;
    private String paymentMethod;
    private BigDecimal depositAmount;
}