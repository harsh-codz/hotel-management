package com.hotel.admin.app.dto.dashboard;


import lombok.Builder; // Using Builder pattern for easier construction
import lombok.Getter;
import lombok.Setter;

import java.util.Map;

@Getter @Setter @Builder // Lombok Builder simplifies object creation
public class DashboardOverviewResponse {
    // Booking Metrics
    private long totalBookingsToday;
    private long totalBookingsThisWeek; // Consider defining "week" (e.g., Mon-Sun)
    private long totalBookingsThisMonth;
    private long currentlyCheckedIn; // Count of CHECKED_IN bookings

    // Room Metrics
    private long totalRooms;
    private long availableRoomsNow; // Requires calculation (Total - Occupied - Maintenance)
    private long occupiedRoomsNow;
    private long roomsUnderMaintenance;

    // Other Potential Metrics (Add as needed)
    private long pendingComplaints; // Count of OPEN or IN_PROGRESS complaints
    private long totalStaff;
    private long totalCustomers;

    // Data for Charts (Example: Bookings per day for the last 7 days)
    private Map<String, Long> recentBookingCounts; // Key: Date String (e.g., "YYYY-MM-DD"), Value: Count
}
