package com.hotel.admin.app.repository;

import com.hotel.admin.app.entity.Booking;
import com.hotel.admin.app.entity.enums.BookingStatus;

// Corrected enum import path assuming it's in model.enums
import org.springframework.data.domain.Page; // Keep if custom repo returns Page
import org.springframework.data.domain.Pageable; // Keep if custom repo returns Page
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository; // Add @Repository annotation

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

@Repository // Add annotation
public interface BookingRepository extends JpaRepository<Booking,Long>, BookingRepositoryCustom { // Remove JpaSpecificationExecutor if not used

    // --- Derived Queries for Dashboard/Validation ---

    // Count bookings with check-in date today (Example - might not be needed if using date ranges)
    // long countByCheckInDate(LocalDate date); // Keep if specifically needed

    // Count bookings *made* within a specific LocalDateTime range
    long countByBookingDateBetween(LocalDateTime startOfDay, LocalDateTime endOfDay);

    // Count by status (e.g., currently checked in)
    long countByStatus(BookingStatus status);

    // Check for validation before room update/delete
    boolean existsByRoomIdAndStatusInAndCheckOutDateAfter(
            Long roomId,
            List<BookingStatus> statuses,
            LocalDate date
    );

    // --- Custom Query for Availability Check ---

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
            @Param("excludedBookingId") Long excludedBookingId,
            @Param("conflictingStatuses") List<BookingStatus> conflictingStatuses);

    // Convenience overload (remains correct)
    default boolean findConflictingBookings(Long roomId, LocalDate startDate, LocalDate endDate, List<BookingStatus> conflictingStatuses) {
        return findConflictingBookings(roomId, startDate, endDate, null, conflictingStatuses);
    }

    // --- Custom Query for Dashboard Chart Data ---

    // Renamed method and corrected return type/parameter name
    @Query("SELECT b.bookingDate, COUNT(b.id) " +
           "FROM Booking b WHERE b.bookingDate >= :startDate " +
           "GROUP BY b.bookingDate ORDER BY b.bookingDate")
    List<Object[]> getBookingCountsFromDate( // Renamed method for clarity
        @Param("startDate") LocalDateTime startDate // Matched param name to query
    );

    // --- REMOVED Methods ---
    // Removed: Page<Booking> findAll(Specification<Booking> spec, Pageable pageable); -> Redundant if not using SpecExecutor
    // Removed: List<DateCount> countBookingsGroupedByDate(LocalDateTime sevenDaysAgo); -> Incorrect signature/approach
    // Removed: List<Object[]> getBookingTimestampsAndCounts(LocalDateTime sevenDaysAgo); -> Replaced by getBookingCountsFromDate with @Query

    // Note: The actual implementation for filtering without specs is now expected in
    // BookingRepositoryCustomImpl using the findWithDynamicQuery method.
}