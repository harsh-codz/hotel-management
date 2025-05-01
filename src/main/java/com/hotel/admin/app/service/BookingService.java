package com.hotel.admin.app.service;

import com.hotel.admin.app.dto.booking.BookingRequest;
import com.hotel.admin.app.dto.booking.BookingResponse;
import com.hotel.admin.app.dto.booking.BookingUpdateRequest;
import com.hotel.admin.app.dto.user.UserBasicInfoResponse; // Import basic user DTO
import com.hotel.admin.app.exception.BadRequestException;
import com.hotel.admin.app.exception.ConflictException;
import com.hotel.admin.app.exception.ResourceNotFoundException;
import com.hotel.admin.app.entity.*;// Import entities
import com.hotel.admin.app.entity.enums.BookingStatus;
import com.hotel.admin.app.repository.BookingRepository; // Import repositories
import com.hotel.admin.app.repository.RoomRepository;
import com.hotel.admin.app.repository.UserRepository;
import com.hotel.admin.app.service.RoomService; // To reuse room mapping logic
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;


import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;
import java.util.Arrays;
import java.util.List;
import java.util.UUID; // For generating booking ID


@Service
@RequiredArgsConstructor
public class BookingService {

    private static final Logger log = LoggerFactory.getLogger(BookingService.class);

    private final BookingRepository bookingRepository;
    private final RoomRepository roomRepository;
    private final UserRepository userRepository;
    private final RoomService roomService; // Inject RoomService for mapping RoomResponse

    // Define statuses that represent a conflict for availability checks
    private static final List<BookingStatus> CONFLICTING_STATUSES = Arrays.asList(
            BookingStatus.CONFIRMED,
            BookingStatus.CHECKED_IN
            // Add PENDING_CONFIRMATION if those should also block availability
    );

    // US017: Create Booking
    @Transactional
    public BookingResponse createBooking(BookingRequest request) {
        log.info("Attempting to create booking for room ID {} and user ID {}", request.getRoomId(), request.getUserId());

        validateBookingDates(request.getCheckInDate(), request.getCheckOutDate());

        User user = userRepository.findById(request.getUserId())
                .orElseThrow(() -> new BadRequestException("Invalid User (Customer) ID: " + request.getUserId()));
        // TODO: Check if user role is CUSTOMER?

        Room room = roomRepository.findById(request.getRoomId())
                .orElseThrow(() -> new BadRequestException("Invalid Room ID: " + request.getRoomId()));

        // --- Critical Availability Check ---
        checkRoomAvailability(room.getId(), request.getCheckInDate(), request.getCheckOutDate(), null); // null for excluded ID

        // Validation: Check occupancy
        if (request.getNumberOfGuests() > room.getMaxOccupancy()) {
            throw new BadRequestException("Number of guests (" + request.getNumberOfGuests() +
                    ") exceeds maximum occupancy (" + room.getMaxOccupancy() + ") for room " + room.getRoomNumber());
        }

        Booking booking = new Booking();
        booking.setUser(user);
        booking.setRoom(room);
        booking.setCheckInDate(request.getCheckInDate());
        booking.setCheckOutDate(request.getCheckOutDate());
        booking.setNumberOfGuests(request.getNumberOfGuests());
        booking.setSpecialRequests(request.getSpecialRequests());
        booking.setPaymentMethod(request.getPaymentMethod());
        booking.setDepositAmount(request.getDepositAmount());

        // Set calculated/default fields
        booking.setStatus(BookingStatus.CONFIRMED); // Default status for new admin booking
        booking.setBookingDate(LocalDateTime.now());
        booking.setTotalAmount(calculateTotalAmount(room.getPricePerNight(), request.getCheckInDate(), request.getCheckOutDate()));
        booking.setBookingId(generateBookingId()); // Generate a unique ID

        Booking savedBooking = bookingRepository.save(booking);
        log.info("Successfully created booking ID: {}", savedBooking.getId());

        // Consider changing room status if applicable (e.g., if check-in is today)
        // This logic might belong in a separate check-in process

        return mapToBookingResponse(savedBooking);
    }

    // US018: Get Booking by ID
    public BookingResponse getBookingById(Long id) {
        Booking booking = bookingRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Booking", "id", id));
        return mapToBookingResponse(booking);
    }
    public Page<BookingResponse> findBookingsByCriteria(
        LocalDate checkInFrom, LocalDate checkInTo,
        LocalDate checkOutFrom, LocalDate checkOutTo,
        Long roomTypeId, List<BookingStatus> statuses,
        String customerName, String roomNumber, String bookingId,
        Pageable pageable) {

    // This method now needs to call a repository method capable of handling dynamic criteria
    Page<Booking> bookingPage = bookingRepository.findWithDynamicQuery( // Or a better name
            checkInFrom, checkInTo, checkOutFrom, checkOutTo,
            roomTypeId, statuses, customerName, roomNumber, bookingId,
            pageable
    );

    return bookingPage.map(this::mapToBookingResponse); // Reuse existing mapping logic
}
     // US017: Update Booking (Simplified - doesn't allow changing room/user)
     @Transactional
     public BookingResponse updateBooking(Long id, BookingUpdateRequest request) {
         log.info("Attempting to update booking ID: {}", id);
         Booking existingBooking = bookingRepository.findById(id)
                 .orElseThrow(() -> new ResourceNotFoundException("Booking", "id", id));

         validateBookingDates(request.getCheckInDate(), request.getCheckOutDate());

         // --- Check Availability for new dates, excluding the current booking ---
         checkRoomAvailability(existingBooking.getRoom().getId(), request.getCheckInDate(), request.getCheckOutDate(), existingBooking.getId());

         // Validation: Check occupancy (using existing room's max occupancy)
         if (request.getNumberOfGuests() > existingBooking.getRoom().getMaxOccupancy()) {
             throw new BadRequestException("Number of guests (" + request.getNumberOfGuests() +
                     ") exceeds maximum occupancy (" + existingBooking.getRoom().getMaxOccupancy() + ") for room " + existingBooking.getRoom().getRoomNumber());
         }

         // Apply updates
         existingBooking.setCheckInDate(request.getCheckInDate());
         existingBooking.setCheckOutDate(request.getCheckOutDate());
         existingBooking.setNumberOfGuests(request.getNumberOfGuests());
         existingBooking.setSpecialRequests(request.getSpecialRequests());
         existingBooking.setPaymentMethod(request.getPaymentMethod());
         existingBooking.setDepositAmount(request.getDepositAmount());
         // Recalculate amount if dates changed
         existingBooking.setTotalAmount(calculateTotalAmount(existingBooking.getRoom().getPricePerNight(), request.getCheckInDate(), request.getCheckOutDate()));
         // Status is likely handled separately (e.g., cannot update a CHECKED_IN booking this way)
         validateUpdateEligibility(existingBooking.getStatus());


         Booking updatedBooking = bookingRepository.save(existingBooking);
         log.info("Successfully updated booking ID: {}", updatedBooking.getId());
         return mapToBookingResponse(updatedBooking);
     }


     // US017: Cancel Booking
     @Transactional
     public void cancelBooking(Long id) {
         log.info("Attempting to cancel booking ID: {}", id);
         Booking booking = bookingRepository.findById(id)
                 .orElseThrow(() -> new ResourceNotFoundException("Booking", "id", id));

         validateCancellationEligibility(booking.getStatus());

         booking.setStatus(BookingStatus.CANCELLED);
         bookingRepository.save(booking);
         log.info("Successfully cancelled booking ID: {}", id);

         // Optional: If cancelled far enough in advance, maybe change Room status back to AVAILABLE?
         // Room room = booking.getRoom();
         // if (room.getStatus() == RoomStatus.OCCUPIED based on this booking?) { ... }
     }


    // --- Helper Methods ---

    private void checkRoomAvailability(Long roomId, LocalDate startDate, LocalDate endDate, Long excludedBookingId) {
        boolean isConflicting = bookingRepository.findConflictingBookings(
                roomId, startDate, endDate, excludedBookingId, CONFLICTING_STATUSES
        );
        if (isConflicting) {
            throw new ConflictException("Room ID " + roomId + " is not available for the selected dates (" + startDate + " to " + endDate + ").");
        }
         log.debug("Availability check passed for room ID {} between {} and {} (excluding booking ID {})", roomId, startDate, endDate, excludedBookingId);
    }


    private void validateBookingDates(LocalDate checkIn, LocalDate checkOut) {
        if (!checkOut.isAfter(checkIn)) {
            throw new BadRequestException("Check-out date must be after check-in date.");
        }
        // Add other date validations if needed (e.g., max booking duration)
    }

    private void validateCancellationEligibility(BookingStatus currentStatus) {
         if (currentStatus == BookingStatus.CHECKED_IN || currentStatus == BookingStatus.CHECKED_OUT || currentStatus == BookingStatus.CANCELLED) {
             throw new ConflictException("Cannot cancel a booking with status: " + currentStatus);
         }
    }

    private void validateUpdateEligibility(BookingStatus currentStatus) {
         // Example: Prevent updates if already checked in or out
        if (currentStatus == BookingStatus.CHECKED_IN || currentStatus == BookingStatus.CHECKED_OUT) {
             throw new ConflictException("Cannot update booking details for a booking with status: " + currentStatus);
         }
    }


    private BigDecimal calculateTotalAmount(BigDecimal pricePerNight, LocalDate checkIn, LocalDate checkOut) {
        long numberOfNights = ChronoUnit.DAYS.between(checkIn, checkOut);
        if (numberOfNights <= 0) {
            return BigDecimal.ZERO; // Or handle as error?
        }
        return pricePerNight.multiply(BigDecimal.valueOf(numberOfNights));
    }

     private String generateBookingId() {
        // Simple example using UUID - can customize format
        return "BK-" + UUID.randomUUID().toString().substring(0, 8).toUpperCase();
    }


    // Manual Mapping (Example - Consider MapStruct)
    private BookingResponse mapToBookingResponse(Booking booking) {
        BookingResponse response = new BookingResponse();
        response.setId(booking.getId());
        response.setBookingId(booking.getBookingId());
        response.setCheckInDate(booking.getCheckInDate());
        response.setCheckOutDate(booking.getCheckOutDate());
        response.setNumberOfGuests(booking.getNumberOfGuests());
        response.setStatus(booking.getStatus());
        response.setBookingDate(booking.getBookingDate());
        response.setTotalAmount(booking.getTotalAmount());
        response.setSpecialRequests(booking.getSpecialRequests());
        response.setPaymentMethod(booking.getPaymentMethod());
        response.setDepositAmount(booking.getDepositAmount());

        if (booking.getUser() != null) {
            response.setUser(mapToUserBasicInfoResponse(booking.getUser()));
        }
        if (booking.getRoom() != null) {
            // Reuse the mapping logic from RoomService by calling its public mapping method
            // Or duplicate the mapping logic here if preferred
            response.setRoom(roomService.mapToRoomResponse(booking.getRoom())); // Assuming mapToRoomResponse is public in RoomService
        }
        return response;
    }

    private UserBasicInfoResponse mapToUserBasicInfoResponse(User user) {
        return new UserBasicInfoResponse(
                user.getId(),
                user.getUsername(),
                user.getFullName(), // Ensure User entity has fullName
                user.getEmail()
        );
    }
}