package com.hotel.admin.app.repository;

import com.hotel.admin.app.entity.Booking;
import com.hotel.admin.app.entity.enums.BookingStatus;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import java.time.LocalDate;
import java.util.List;

public interface BookingRepositoryCustom {
    Page<Booking> findWithDynamicQuery(
            LocalDate checkInFrom, LocalDate checkInTo,
            LocalDate checkOutFrom, LocalDate checkOutTo,
            Long roomTypeId, List<BookingStatus> statuses,
            String customerName, String roomNumber, String bookingId,
            Pageable pageable);
}