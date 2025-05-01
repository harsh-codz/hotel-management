package com.hotel.admin.app.entity;

import jakarta.persistence.*;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;

import java.time.LocalDate;
import java.time.LocalDateTime;

import com.hotel.admin.app.entity.enums.BookingStatus;

import java.math.BigDecimal;

@Entity
@Table(name = "booking")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class Booking {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    // US017 implies a unique Reservation ID. Let's make this a generated field in the service.
    @Column(nullable = false, unique = true)
    private String bookingId;

    @ManyToOne(fetch = FetchType.LAZY) // Lazy loading for User (customer)
    @JoinColumn(name = "user_id", nullable = false) // Mandatory (US017)
    private User user; // The customer who made the booking

    @ManyToOne(fetch = FetchType.EAGER) // Eager loading for Room is often useful here
    @JoinColumn(name = "room_id", nullable = false) // Mandatory (US017)
    private Room room;

    @Column(nullable = false) // Mandatory (US017)
    private LocalDate checkInDate;

    @Column(nullable = false) // Mandatory (US017)
    private LocalDate checkOutDate;

    @Column(nullable = false) // Mandatory (US017), positive integer validation
    private Integer numberOfGuests;

    @Enumerated(EnumType.STRING)
    @Column(length = 30, nullable = false)
    private BookingStatus status; // (US017, US018)

    @Column(nullable = false) // Mandatory (US018)
    private LocalDateTime bookingDate; // Timestamp when the booking was created

    private BigDecimal totalAmount; // Can be calculated, might be nullable initially (US017, US018)

    private String specialRequests; // Optional (US017)

    private String paymentMethod; // Optional, but mandatory in form? Let's make it nullable for flexibility (US017)
    private BigDecimal depositAmount; // Optional (US017)

    
   
}