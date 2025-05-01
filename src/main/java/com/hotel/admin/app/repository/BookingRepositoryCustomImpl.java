package com.hotel.admin.app.repository;

import com.hotel.admin.app.entity.*; // Import needed entities (Booking, Room, RoomType, User)
import com.hotel.admin.app.entity.enums.BookingStatus;

import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import jakarta.persistence.TypedQuery;
import jakarta.persistence.criteria.*;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Repository; // Optional, often not needed for impl

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

public class BookingRepositoryCustomImpl implements BookingRepositoryCustom {

    @PersistenceContext
    private EntityManager em;

    @Override
    public Page<Booking> findWithDynamicQuery(
            LocalDate checkInFrom, LocalDate checkInTo, LocalDate checkOutFrom, LocalDate checkOutTo,
            Long roomTypeId, List<BookingStatus> statuses, String customerName, String roomNumber, String bookingId,
            Pageable pageable) {

        CriteriaBuilder cb = em.getCriteriaBuilder();
        // --- Query for retrieving Booking entities ---
        CriteriaQuery<Booking> query = cb.createQuery(Booking.class);
        Root<Booking> booking = query.from(Booking.class);
        // Join necessary tables for filtering/sorting
        Join<Booking, Room> room = booking.join("room", JoinType.LEFT); // Use LEFT if room might be null (unlikely for booking)
        Join<Room, RoomType> roomType = room.join("roomType", JoinType.LEFT);
        Join<Booking, User> user = booking.join("user", JoinType.LEFT);

        List<Predicate> predicates = new ArrayList<>();

        // --- Build predicates dynamically based on parameters ---
        if (checkInFrom != null) {
            predicates.add(cb.greaterThanOrEqualTo(booking.get("checkInDate"), checkInFrom));
        }
        if (checkInTo != null) {
            predicates.add(cb.lessThanOrEqualTo(booking.get("checkInDate"), checkInTo));
        }
        if (checkOutFrom != null) {
             predicates.add(cb.greaterThanOrEqualTo(booking.get("checkOutDate"), checkOutFrom));
        }
         if (checkOutTo != null) {
             predicates.add(cb.lessThanOrEqualTo(booking.get("checkOutDate"), checkOutTo));
         }
        if (roomTypeId != null) {
            predicates.add(cb.equal(roomType.get("id"), roomTypeId));
        }
        if (statuses != null && !statuses.isEmpty()) {
            predicates.add(booking.get("status").in(statuses));
        }
        if (customerName != null && !customerName.isBlank()) {
            predicates.add(cb.like(cb.lower(user.get("fullName")), "%" + customerName.toLowerCase() + "%"));
        }
        if (roomNumber != null && !roomNumber.isBlank()) {
            predicates.add(cb.equal(room.get("roomNumber"), roomNumber));
        }
         if (bookingId != null && !bookingId.isBlank()) {
            predicates.add(cb.equal(booking.get("bookingId"), bookingId));
        }
        // --- End building predicates ---

        query.where(cb.and(predicates.toArray(new Predicate[0])));

        // --- Apply Sorting ---
        List<Order> orders = new ArrayList<>();
         if (pageable.getSort().isSorted()) {
             for (Sort.Order order : pageable.getSort()) {
                 // Handle sorting based on path, e.g., "user.fullName", "room.roomNumber"
                 // Requires parsing the property path and applying cb.asc/cb.desc
                 // Simple example for direct booking properties:
                 try {
                      Path<?> sortPath = parsePath(booking, order.getProperty()); // Implement parsePath helper if needed
                      orders.add(order.isAscending() ? cb.asc(sortPath) : cb.desc(sortPath));
                 } catch (IllegalArgumentException e) {
                     // Log warning: cannot sort by property order.getProperty()
                     System.err.println("Cannot sort by property: " + order.getProperty());
                 }
             }
         } else {
             // Default sort if none provided
             orders.add(cb.desc(booking.get("bookingDate")));
         }
         query.orderBy(orders);


        TypedQuery<Booking> typedQuery = em.createQuery(query);

        // --- Apply Pagination ---
        typedQuery.setFirstResult((int) pageable.getOffset());
        typedQuery.setMaxResults(pageable.getPageSize());

        List<Booking> resultList = typedQuery.getResultList();

        // --- Query for total count (for pagination) ---
         CriteriaQuery<Long> countQuery = cb.createQuery(Long.class);
         Root<Booking> countRoot = countQuery.from(Booking.class);
         // Re-join and apply the *same* predicates for accurate count
         Join<Booking, Room> countRoom = countRoot.join("room", JoinType.LEFT);
         Join<Room, RoomType> countRoomType = countRoom.join("roomType", JoinType.LEFT);
         Join<Booking, User> countUser = countRoot.join("user", JoinType.LEFT);
         countQuery.select(cb.count(countRoot)).where(cb.and(predicates.toArray(new Predicate[0]))); // Reuse predicates

         Long totalCount = em.createQuery(countQuery).getSingleResult();


        return new PageImpl<>(resultList, pageable, totalCount);
    }

    // Helper to parse sort paths (simplistic example, needs more robust implementation for nested paths)
     private Path<?> parsePath(Root<?> root, String propertyPath) {
         String[] parts = propertyPath.split("\\.");
         Path<?> path = root;
         for (String part : parts) {
             path = path.get(part);
         }
         return path;
     }
}