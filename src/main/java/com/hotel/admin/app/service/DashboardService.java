package com.hotel.admin.app.service;


import com.hotel.admin.app.dto.dashboard.DashboardOverviewResponse;
import com.hotel.admin.app.dto.dashboard.DateCount; // Import the record/DTO
import com.hotel.admin.app.entity.enums.BookingStatus;
import com.hotel.admin.app.entity.enums.ComplaintStatus;
import com.hotel.admin.app.entity.enums.RoomStatus;
import com.hotel.admin.app.repository.BookingRepository;
import com.hotel.admin.app.repository.ComplaintRepository;
import com.hotel.admin.app.repository.RoomRepository;
import com.hotel.admin.app.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional; // Read-only transaction is good practice

import java.time.DayOfWeek;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.temporal.TemporalAdjusters;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class DashboardService {

    private static final Logger log = LoggerFactory.getLogger(DashboardService.class);

    private final BookingRepository bookingRepository;
    private final RoomRepository roomRepository;
    private final UserRepository userRepository; // Inject if counting users
    private final ComplaintRepository complaintRepository; // Inject if counting complaints

    @Transactional(readOnly = true) // Use read-only transaction for queries
    public DashboardOverviewResponse getDashboardOverview() {
        log.debug("Fetching dashboard overview data.");

        LocalDate today = LocalDate.now();
        LocalDateTime startOfToday = today.atStartOfDay();
        LocalDateTime endOfToday = today.atTime(LocalTime.MAX);

        // --- Booking Metrics ---
        // Define week/month boundaries (adjust logic as needed, e.g., week starts Monday)
        LocalDate startOfWeek = today.with(TemporalAdjusters.previousOrSame(DayOfWeek.MONDAY));
        LocalDate endOfWeek = today.with(TemporalAdjusters.nextOrSame(DayOfWeek.SUNDAY));
        LocalDateTime startOfWeekTime = startOfWeek.atStartOfDay();
        LocalDateTime endOfWeekTime = endOfWeek.atTime(LocalTime.MAX);

        LocalDate startOfMonth = today.withDayOfMonth(1);
        LocalDate endOfMonth = today.with(TemporalAdjusters.lastDayOfMonth());
        LocalDateTime startOfMonthTime = startOfMonth.atStartOfDay();
        LocalDateTime endOfMonthTime = endOfMonth.atTime(LocalTime.MAX);

        // Example: Counting bookings *created* within the period
        long bookingsToday = bookingRepository.countByBookingDateBetween(startOfToday, endOfToday);
        long bookingsThisWeek = bookingRepository.countByBookingDateBetween(startOfWeekTime, endOfWeekTime);
        long bookingsThisMonth = bookingRepository.countByBookingDateBetween(startOfMonthTime, endOfMonthTime);
        long currentlyCheckedIn = bookingRepository.countByStatus(BookingStatus.CHECKED_IN);

        // --- Room Metrics ---
        long totalRooms = roomRepository.count();
        long occupiedRooms = roomRepository.countByRoomStatus(RoomStatus.OCCUPIED);
        long maintenanceRooms = roomRepository.countByRoomStatus(RoomStatus.UNDER_MAINTENANCE);
        // Calculate available based on other counts
        long availableRooms = totalRooms - occupiedRooms - maintenanceRooms;

        // --- Other Metrics (Optional) ---
        long pendingComplaints = complaintRepository.countByStatusIn(
                Arrays.asList(ComplaintStatus.OPEN, ComplaintStatus.IN_PROGRESS, ComplaintStatus.REOPENED)
        );
        long totalStaff = userRepository.countByRoleName("ROLE_STAFF");
        long totalCustomers = userRepository.countByRoleName("ROLE_CUSTOMER");

        // --- Chart Data ---
      
LocalDateTime sevenDaysAgo = startOfToday.minusDays(7);
// Call the new repository method
List<Object[]> rawCounts = bookingRepository.getBookingCountsFromDate(sevenDaysAgo);

// Process the results in Java using Streams
Map<String, Long> recentBookingCountsMap = rawCounts.stream()
        .collect(Collectors.groupingBy(
                // Key Mapper: Extract LocalDate from LocalDateTime (Object[0]) and convert to String
                result -> ((LocalDateTime) result[0]).toLocalDate().toString(),
                // Value Mapper: Sum the counts (Object[1] which is Long) for each date
                Collectors.summingLong(result -> (Long) result[1])
        ));

        // --- Build Response DTO ---
        return DashboardOverviewResponse.builder()
                .totalBookingsToday(bookingsToday)
                .totalBookingsThisWeek(bookingsThisWeek)
                .totalBookingsThisMonth(bookingsThisMonth)
                .currentlyCheckedIn(currentlyCheckedIn)
                .totalRooms(totalRooms)
                .availableRoomsNow(availableRooms) // Use calculated value
                .occupiedRoomsNow(occupiedRooms)
                .roomsUnderMaintenance(maintenanceRooms)
                .pendingComplaints(pendingComplaints)
                .totalStaff(totalStaff)
                .totalCustomers(totalCustomers)
                .recentBookingCounts(recentBookingCountsMap)
                .build();
    }
}