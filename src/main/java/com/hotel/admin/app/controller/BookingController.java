package com.hotel.admin.app.controller;

import com.hotel.admin.app.dto.booking.BookingRequest;
import com.hotel.admin.app.dto.booking.BookingResponse;
import com.hotel.admin.app.dto.booking.BookingUpdateRequest;
import com.hotel.admin.app.entity.enums.BookingStatus;
// Removed Specification import
// Import enum if filtering by it
import com.hotel.admin.app.service.BookingService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
// Removed Specification related imports
import org.springframework.format.annotation.DateTimeFormat; // For date parsing
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.util.List; // For status list

@RestController
@RequestMapping("/api/admin/bookings")
@RequiredArgsConstructor
@PreAuthorize("hasRole('ADMIN')")
public class BookingController {

    private final BookingService bookingService;

    // POST /api/admin/bookings - US017 (Remains the same)
    @PostMapping
    public ResponseEntity<BookingResponse> createBooking(@Valid @RequestBody BookingRequest bookingRequest) {
        BookingResponse createdBooking = bookingService.createBooking(bookingRequest);
        return ResponseEntity.status(HttpStatus.CREATED).body(createdBooking);
    }

    // GET /api/admin/bookings/{id} - US018 (Remains the same)
    @GetMapping("/{id}")
    public ResponseEntity<BookingResponse> getBookingById(@PathVariable Long id) {
        return ResponseEntity.ok(bookingService.getBookingById(id));
    }

    // GET /api/admin/bookings - US018 (Search/Filtering without Specification)
    @GetMapping
    public ResponseEntity<Page<BookingResponse>> findBookings(
            // --- Receive individual filter parameters ---
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate checkInFrom,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate checkInTo,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate checkOutFrom,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate checkOutTo,
            @RequestParam(required = false) Long roomTypeId,
            @RequestParam(required = false) List<BookingStatus> status, // Allows multiple ?status=CONFIRMED&status=CHECKED_IN
            @RequestParam(required = false) String customerName,
            @RequestParam(required = false) String roomNumber,
            @RequestParam(required = false) String bookingId,
            // --- End filter parameters ---
            Pageable pageable // Spring handles pagination/sorting params automatically
    ) {
        // Pass parameters down to the service layer
        Page<BookingResponse> bookings = bookingService.findBookingsByCriteria(
                checkInFrom, checkInTo, checkOutFrom, checkOutTo,
                roomTypeId, status, customerName, roomNumber, bookingId,
                pageable
        );
        return ResponseEntity.ok(bookings);
    }

    // PUT /api/admin/bookings/{id} - US017 (Update - Remains the same)
    @PutMapping("/{id}")
    public ResponseEntity<BookingResponse> updateBooking(@PathVariable Long id, @Valid @RequestBody BookingUpdateRequest bookingUpdateRequest) {
        BookingResponse updatedBooking = bookingService.updateBooking(id, bookingUpdateRequest);
        return ResponseEntity.ok(updatedBooking);
    }

    // PUT /api/admin/bookings/{id}/cancel - US017 (Cancel - Remains the same)
    @PutMapping("/{id}/cancel")
    public ResponseEntity<Void> cancelBooking(@PathVariable Long id) {
        bookingService.cancelBooking(id);
        return ResponseEntity.noContent().build();
    }
}