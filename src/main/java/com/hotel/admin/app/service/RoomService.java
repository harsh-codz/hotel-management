package com.hotel.admin.app.service;

import com.hotel.admin.app.dto.room.*;
import com.hotel.admin.app.entity.Amenity;
import com.hotel.admin.app.entity.Room;
import com.hotel.admin.app.entity.RoomType;
import com.hotel.admin.app.exception.BadRequestException;
import com.hotel.admin.app.exception.ConflictException;
import com.hotel.admin.app.exception.ResourceNotFoundException;
 // Import entities
import com.hotel.admin.app.entity.*;
import com.hotel.admin.app.entity.enums.BookingStatus;
import com.hotel.admin.app.entity.enums.RoomStatus;
import com.hotel.admin.app.repository.*; // Import repositories
import com.opencsv.CSVReader; // If using OpenCSV
import com.opencsv.exceptions.CsvValidationException;

import jakarta.persistence.criteria.Join;
import jakarta.persistence.criteria.Predicate;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.io.InputStreamReader;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.*;
import java.util.stream.Collectors;
import java.io.IOException;

@Service
@RequiredArgsConstructor // Lombok constructor injection
public class RoomService {

    private static final Logger log = LoggerFactory.getLogger(RoomService.class);

    private final RoomRepository roomRepository;
    private final RoomTypeRepository roomTypeRepository;
    private final AmenityRepository amenityRepository;
    private final BookingRepository bookingRepository;
    // private final RoomMapper roomMapper; // Optional: Use MapStruct or similar for mapping

    // --- Lookup Methods ---

    public List<RoomTypeResponse> getAllRoomTypes() {
        return roomTypeRepository.findAll().stream()
                .map(this::mapToRoomTypeResponse)
                .collect(Collectors.toList());
    }

    public List<AmenityResponse> getAllAmenities() {
        return amenityRepository.findAll().stream()
                .map(this::mapToAmenityResponse)
                .collect(Collectors.toList());
    }

    // --- Core CRUD ---

    // US015: Get single room
    public RoomResponse getRoomById(Long id) {
        Room room = roomRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Room", "id", id));
        return mapToRoomResponse(room);
    }

    // US015: List/Search rooms (Simplified filtering example, Specification is better)
    public Page<RoomResponse> findRooms(
        String roomNumber,
        Long roomTypeId,
        RoomStatus status,
        Pageable pageable) {

    // Build Specification dynamically based on provided filters
    Specification<Room> spec = (root, query, criteriaBuilder) -> {
        // Use a list to hold all the predicates (conditions)
        List<Predicate> predicates = new ArrayList<>();

        // Add predicate for roomNumber if provided
        if (roomNumber != null && !roomNumber.trim().isEmpty()) {
            predicates.add(criteriaBuilder.equal(root.get("roomNumber"), roomNumber.trim()));
        }

        // Add predicate for roomTypeId if provided (requires a join)
        if (roomTypeId != null) {
            Join<Room, RoomType> roomTypeJoin = root.join("roomType"); // Join Room with RoomType entity
            predicates.add(criteriaBuilder.equal(roomTypeJoin.get("id"), roomTypeId));
        }

        // Add predicate for roomStatus if provided
        if (status != null) {
            predicates.add(criteriaBuilder.equal(root.get("roomStatus"), status));
        }

        // TODO: Add predicates for other filters (price range, amenities, availability) here
        // Example for price range:
        // if (minPrice != null) {
        //     predicates.add(criteriaBuilder.greaterThanOrEqualTo(root.get("pricePerNight"), minPrice));
        // }
        // if (maxPrice != null) {
        //     predicates.add(criteriaBuilder.lessThanOrEqualTo(root.get("pricePerNight"), maxPrice));
        // }
         // Example for amenities (more complex, requires subquery or checking join table size):
         // if (amenityIds != null && !amenityIds.isEmpty()) {
         //    // ... criteriaBuilder logic for filtering by joined amenities ...
         // }


        // Combine all predicates with AND
        return criteriaBuilder.and(predicates.toArray(new Predicate[0]));
    };

    // Execute the query with the dynamically built Specification and Pageable
    Page<Room> roomPage = roomRepository.findAll(spec, pageable);

    // Map the results to the response DTO
    return roomPage.map(this::mapToRoomResponse);
}

    // US016: Add new room
    @Transactional
    public RoomResponse createRoom(RoomRequest request) {
        log.info("Attempting to create room with number: {}", request.getRoomNumber());

        // Validation: Check room number uniqueness
        if (roomRepository.existsByRoomNumber(request.getRoomNumber())) {
            throw new ConflictException("Room number '" + request.getRoomNumber() + "' already exists.");
        }

        RoomType roomType = roomTypeRepository.findById(request.getRoomTypeId())
                .orElseThrow(() -> new BadRequestException("Invalid RoomType ID: " + request.getRoomTypeId()));

        Set<Amenity> amenities = findAndValidateAmenities(request.getAmenityIds());

        Room room = new Room();
        mapRequestToEntity(request, room, roomType, amenities); // Use helper mapping method

        Room savedRoom = roomRepository.save(room);
        log.info("Successfully created room ID: {}", savedRoom.getId());
        return mapToRoomResponse(savedRoom);
    }

    // US016: Update room
    @Transactional
    public RoomResponse updateRoom(Long id, RoomRequest request) {
        log.info("Attempting to update room ID: {}", id);
        Room existingRoom = roomRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Room", "id", id));

        // Validation: Check room number uniqueness (if changed, though usually not allowed)
        // For robustness, let's check even if UI prevents edit - ensure request matches stored or check uniqueness if different
        if (!existingRoom.getRoomNumber().equals(request.getRoomNumber()) &&
            roomRepository.existsByRoomNumberAndIdNot(request.getRoomNumber(), id)) {
             throw new ConflictException("Room number '" + request.getRoomNumber() + "' already exists for another room.");
        }

        // US015 Validation: Check for active bookings before allowing critical changes
        // Define which changes are "critical" (e.g., type, status to Maintenance if booked)
        // Simple check: Block *any* update if active/upcoming booking exists
        if (hasActiveOrUpcomingBookings(id)) {
            log.warn("Update blocked for room ID {}: Active or upcoming bookings found.", id);
            throw new ConflictException("Cannot update room with active or upcoming bookings.");
        }

        // US015 Validation: Prevent changing status from OCCUPIED directly via this endpoint?
        // This might be handled by separate check-in/out flows. Assume Occupied status is managed elsewhere for now.
        /* if (existingRoom.getRoomStatus() == RoomStatus.OCCUPIED && request.getRoomStatus() != RoomStatus.OCCUPIED) {
             throw new ConflictException("Cannot change status of an occupied room via this endpoint.");
         }*/


        RoomType roomType = roomTypeRepository.findById(request.getRoomTypeId())
                .orElseThrow(() -> new BadRequestException("Invalid RoomType ID: " + request.getRoomTypeId()));

        Set<Amenity> amenities = findAndValidateAmenities(request.getAmenityIds());

        // Apply updates - Room number often non-editable, handled via check above
        mapRequestToEntity(request, existingRoom, roomType, amenities);
        // Explicitly prevent room number change here if desired:
        // existingRoom.setRoomNumber(existingRoom.getRoomNumber()); // Ensure it's not changed by mapRequestToEntity

        Room updatedRoom = roomRepository.save(existingRoom);
        log.info("Successfully updated room ID: {}", updatedRoom.getId());
        return mapToRoomResponse(updatedRoom);
    }

    // US016: Delete room
    @Transactional
    public void deleteRoom(Long id) {
        log.info("Attempting to delete room ID: {}", id);
        Room room = roomRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Room", "id", id));

        // US015 Validation: Check for active bookings before deletion
        if (hasActiveOrUpcomingBookings(id)) {
            log.warn("Deletion blocked for room ID {}: Active or upcoming bookings found.", id);
            throw new ConflictException("Cannot delete room with active or upcoming bookings.");
        }

        roomRepository.delete(room);
        log.info("Successfully deleted room ID: {}", id);
    }

    // US016: Bulk Upload
    @Transactional // Process entire file in one transaction
    public BulkRoomUploadResponse bulkUploadRooms(MultipartFile file) {
        log.info("Starting bulk room upload from file: {}", file.getOriginalFilename());
        List<String> errors = new ArrayList<>();
        int successCount = 0;
        int failureCount = 0;
        int rowNum = 1; // Start from 1 for header

        try (CSVReader reader = new CSVReader(new InputStreamReader(file.getInputStream()))) {
            String[] headers = reader.readNext(); // Read header row - validate if needed
            if (headers == null) throw new BadRequestException("CSV file is empty or invalid.");
            // TODO: Validate headers match expected columns

            String[] line;
            while ((line = reader.readNext()) != null) {
                rowNum++;
                try {
                    // Assuming CSV columns match RoomRequest fields in order:
                    // roomNumber, roomTypeId, pricePerNight, roomStatus, maxOccupancy, description, amenityIds (e.g., "1,2,3")
                    if (line.length < 7) throw new BadRequestException("Insufficient columns"); // Basic check

                    String roomNumber = line[0].trim();
                    Long roomTypeId = Long.parseLong(line[1].trim());
                    BigDecimal price = new BigDecimal(line[2].trim());
                    RoomStatus status = RoomStatus.valueOf(line[3].trim().toUpperCase());
                    Integer occupancy = Integer.parseInt(line[4].trim());
                    String description = line[5].trim();
                    Set<Long> amenityIds = Arrays.stream(line[6].trim().split(","))
                                                 .filter(s -> !s.isEmpty())
                                                 .map(Long::parseLong)
                                                 .collect(Collectors.toSet());

                    // Reuse validation logic where possible, or replicate checks:
                     if (roomRepository.existsByRoomNumber(roomNumber)) {
                        throw new ConflictException("Room number '" + roomNumber + "' already exists.");
                    }
                    RoomType roomType = roomTypeRepository.findById(roomTypeId)
                           .orElseThrow(() -> new BadRequestException("Invalid RoomType ID: " + roomTypeId));
                    Set<Amenity> amenities = findAndValidateAmenities(amenityIds);

                    Room room = new Room();
                    room.setRoomNumber(roomNumber);
                    room.setRoomType(roomType);
                    room.setPricePerNight(price);
                    room.setRoomStatus(status);
                    room.setMaxOccupancy(occupancy);
                    room.setDescription(description);
                    room.setAmenities(amenities);

                    roomRepository.save(room);
                    successCount++;

                } catch ( IllegalArgumentException | BadRequestException | ConflictException e) {
                    log.warn("Error processing CSV row {}: {}", rowNum, e.getMessage());
                    errors.add("Row " + rowNum + ": " + e.getMessage());
                    failureCount++;
                } catch (Exception e) { // Catch unexpected errors
                     log.error("Unexpected error processing CSV row {}: {}", rowNum, e.getMessage(), e);
                     errors.add("Row " + rowNum + ": Unexpected error - " + e.getMessage());
                     failureCount++;
                }
            }
        } catch (IOException | CsvValidationException e) { // Errors reading the file itself
             log.error("Failed to read or process CSV file: {}", e.getMessage(), e);
             // Throwing here cancels the transaction; maybe return response instead?
             throw new BadRequestException("Failed to process CSV file: " + e.getMessage());
        }

        log.info("Bulk upload finished. Success: {}, Failure: {}", successCount, failureCount);
        return new BulkRoomUploadResponse(successCount, failureCount, errors);
    }


    // --- Helper Methods ---

    private boolean hasActiveOrUpcomingBookings(Long roomId) {
        // Check for bookings that are CONFIRMED or CHECKED_IN and end today or later
        List<BookingStatus> activeStatuses = Arrays.asList(BookingStatus.CONFIRMED, BookingStatus.CHECKED_IN);
        return bookingRepository.existsByRoomIdAndStatusInAndCheckOutDateAfter(roomId, activeStatuses, LocalDate.now().minusDays(1)); // Check if checkout is today or later
    }

    private Set<Amenity> findAndValidateAmenities(Set<Long> amenityIds) {
        if (amenityIds == null || amenityIds.isEmpty()) {
            return new HashSet<>();
        }
        Set<Amenity> amenities = new HashSet<>(amenityRepository.findAllById(amenityIds));
        if (amenities.size() != amenityIds.size()) {
            // Find which IDs were invalid
            Set<Long> foundIds = amenities.stream().map(Amenity::getId).collect(Collectors.toSet());
            amenityIds.removeAll(foundIds); // amenityIds now contains only the invalid ones
            throw new BadRequestException("Invalid Amenity ID(s): " + amenityIds);
        }
        return amenities;
    }

    // Manual Mapping (Example - Consider MapStruct)
    private void mapRequestToEntity(RoomRequest request, Room room, RoomType roomType, Set<Amenity> amenities) {
        // Room number might be set only on create or checked for non-editability before calling this
        room.setRoomNumber(request.getRoomNumber());
        room.setRoomType(roomType);
        room.setPricePerNight(request.getPricePerNight());
        room.setRoomStatus(request.getRoomStatus());
        room.setMaxOccupancy(request.getMaxOccupancy());
        room.setDescription(request.getDescription());
        room.setAmenities(amenities);
    }

    RoomResponse mapToRoomResponse(Room room) {
        RoomResponse response = new RoomResponse();
        response.setId(room.getId());
        response.setRoomNumber(room.getRoomNumber());
        response.setPricePerNight(room.getPricePerNight());
        response.setRoomStatus(room.getRoomStatus());
        response.setMaxOccupancy(room.getMaxOccupancy());
        response.setDescription(room.getDescription());
        if (room.getRoomType() != null) {
            response.setRoomType(mapToRoomTypeResponse(room.getRoomType()));
        }
        if (room.getAmenities() != null) {
            response.setAmenities(room.getAmenities().stream()
                    .map(this::mapToAmenityResponse)
                    .collect(Collectors.toSet()));
        }
        return response;
    }

     private RoomTypeResponse mapToRoomTypeResponse(RoomType roomType) {
        return new RoomTypeResponse(roomType.getId(), roomType.getName(), roomType.getDescription());
    }

     private AmenityResponse mapToAmenityResponse(Amenity amenity) {
        return new AmenityResponse(amenity.getId(), amenity.getName());
    }
}