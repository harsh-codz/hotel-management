package com.hotel.admin.app.repository;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import com.hotel.admin.app.dto.dashboard.DateCount;
import com.hotel.admin.app.entity.Booking;
import com.hotel.admin.app.entity.enums.BookingStatus;

public interface BookingRepository extends JpaRepository<Booking,Long>,BookingRepositoryCustom{

  long countByCheckInDate(LocalDate date);

// Count bookings within a date range (e.g., start/end of week/month)
// Note: This might count bookings *made* in the period or *staying* in the period. Define requirement clearly.
// Example: Count bookings *made* today:
long countByBookingDateBetween(LocalDateTime startOfDay, LocalDateTime endOfDay);

// Count by status (e.g., currently checked in)
long countByStatus(BookingStatus status);
    boolean existsByRoomIdAndStatusInAndCheckOutDateAfter(
    Long roomId,
  List<BookingStatus> statuses, // e.g., CONFIRMED, CHECKED_IN
    LocalDate date // e.g., LocalDate.now()
);
   @Query("SELECT COUNT(b) > 0 FROM Booking b " +
           "WHERE b.room.id = :roomId " +
           "AND (:excludedBookingId IS NULL OR b.id <> :excludedBookingId) " +
           "AND b.status IN :conflictingStatuses " +
           "AND b.checkInDate < :endDate " + 
           "AND b.checkOutDate > :startDate") 
           
  boolean findConflictingBookings(
            @Param("roomId") Long roomId,
            @Param("startDate") LocalDate startDate,
            @Param("endDate") LocalDate endDate,
            @Param("excludedBookingId") Long excludedBookingId, // Pass null for new bookings
            @Param("conflictingStatuses") List<BookingStatus> conflictingStatuses);


     // Convenience overload for creating new bookings (no booking ID to exclude)
      default boolean findConflictingBookings(Long roomId, LocalDate startDate, LocalDate endDate, List<BookingStatus> conflictingStatuses) {
        return findConflictingBookings(roomId, startDate, endDate, null, conflictingStatuses);
    }
      Page<Booking> findAll(Specification<Booking> spec, Pageable pageable);
   

      @Query("SELECT NEW com.hotel.admin.app.dto.dashboard.DateCount(FUNCTION('DATE', b.bookingDate), COUNT(b.id)) " +
      "FROM Booking b WHERE b.bookingDate >= :startDate " +
      "GROUP BY FUNCTION('DATE', b.bookingDate) ORDER BY FUNCTION('DATE', b.bookingDate)")
List<DateCount> countBookingsGroupedByDate(@Param("startDate") LocalDateTime startDate);

    }
