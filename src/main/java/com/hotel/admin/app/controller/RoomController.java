package com.hotel.admin.app.controller;

// --- IMPORTS ---
import com.hotel.admin.app.dto.room.*;
import com.hotel.admin.app.entity.enums.RoomStatus;
// Import RoomStatus enum
import com.hotel.admin.app.service.RoomService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
// import org.springframework.data.jpa.domain.Specification; // No longer injecting spec directly
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;
// import com.hotel.admin.app.entity.Room; // No longer needed for spec type hint here
// --- END IMPORTS ---


@RestController
@RequestMapping("/api/admin/rooms")
@RequiredArgsConstructor
@PreAuthorize("hasRole('ADMIN')")
public class RoomController {

    private final RoomService roomService;

    // GET /api/admin/roomtypes - US016 (Helper)
    @GetMapping("/types")
    public ResponseEntity<List<RoomTypeResponse>> getAllRoomTypes() {
        return ResponseEntity.ok(roomService.getAllRoomTypes());
    }

    // GET /api/admin/amenities - US016 (Helper)
    @GetMapping("/amenities")
    public ResponseEntity<List<AmenityResponse>> getAllAmenities() {
        return ResponseEntity.ok(roomService.getAllAmenities());
    }

    // GET /api/admin/rooms/{id} - US015
    @GetMapping("/{id}")
    public ResponseEntity<RoomResponse> getRoomById(@PathVariable Long id) {
        return ResponseEntity.ok(roomService.getRoomById(id));
    }

    // GET /api/admin/rooms - US015 (Filtering/Pagination without spec library)
    @GetMapping
    public ResponseEntity<Page<RoomResponse>> findRooms(
            // Add RequestParam for each filter field
            @RequestParam(required = false) String roomNumber,
            @RequestParam(required = false) Long roomTypeId,
            @RequestParam(required = false) RoomStatus status,
            // TODO: Add params for price range, amenities, availability if needed later
            Pageable pageable) { // Spring automatically provides Pageable (page, size, sort)

        // Pass filter parameters to the service method
        Page<RoomResponse> rooms = roomService.findRooms(
            roomNumber,
            roomTypeId,
            status,
            pageable
        );
        return ResponseEntity.ok(rooms);
    }


    // POST /api/admin/rooms - US016
    @PostMapping
    public ResponseEntity<RoomResponse> createRoom(@Valid @RequestBody RoomRequest roomRequest) {
        RoomResponse createdRoom = roomService.createRoom(roomRequest);
        return ResponseEntity.status(HttpStatus.CREATED).body(createdRoom);
    }

    // PUT /api/admin/rooms/{id} - US016
    @PutMapping("/{id}")
    public ResponseEntity<RoomResponse> updateRoom(@PathVariable Long id, @Valid @RequestBody RoomRequest roomRequest) {
        RoomResponse updatedRoom = roomService.updateRoom(id, roomRequest);
        return ResponseEntity.ok(updatedRoom);
    }

    // DELETE /api/admin/rooms/{id} - US016
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteRoom(@PathVariable Long id) {
        roomService.deleteRoom(id);
        return ResponseEntity.noContent().build(); // Return 204 No Content
    }

     // POST /api/admin/rooms/bulk-upload - US016
     @PostMapping("/bulk-upload")
     public ResponseEntity<BulkRoomUploadResponse> bulkUploadRooms(@RequestParam("file") MultipartFile file) {
         if (file.isEmpty()) {
             return ResponseEntity.badRequest().body(new BulkRoomUploadResponse(0, 0, List.of("Upload file cannot be empty.")));
         }
         BulkRoomUploadResponse response = roomService.bulkUploadRooms(file);
         return ResponseEntity.ok(response);
     }
}