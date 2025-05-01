package com.hotel.admin.app.controller;


import com.hotel.admin.app.dto.bill.BillRequest;
import com.hotel.admin.app.dto.bill.BillResponse;
import com.hotel.admin.app.dto.bill.ServiceResponse;
import com.hotel.admin.app.service.BillService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/admin") // Grouping under admin
@RequiredArgsConstructor
@PreAuthorize("hasRole('ADMIN')")
public class BillController {

    private final BillService billService;

    // GET /api/admin/services - US021 (Helper)
    @GetMapping("/services")
    public ResponseEntity<List<ServiceResponse>> getAllServices() {
        return ResponseEntity.ok(billService.getAllServices());
    }

    // POST /api/admin/bills - US020, US021
    @PostMapping("/bills")
    public ResponseEntity<BillResponse> createBill(@Valid @RequestBody BillRequest billRequest) {
        BillResponse createdBill = billService.createBill(billRequest);
        return ResponseEntity.status(HttpStatus.CREATED).body(createdBill);
    }

    // GET /api/admin/bills/{id} - US020
    @GetMapping("/bills/{id}")
    public ResponseEntity<BillResponse> getBillById(@PathVariable Long id) {
        return ResponseEntity.ok(billService.getBillById(id));
    }

    // GET /api/admin/bills - US020 (List/Search)
    @GetMapping("/bills")
    public ResponseEntity<Page<BillResponse>> findBills(
            // Add @RequestParam for filtering criteria here
            // e.g., @RequestParam(required=false) Long customerId,
            // e.g., @RequestParam(required=false) PaymentStatus status, ...
            Pageable pageable) {
        // Pass criteria to a service method designed for filtering
        Page<BillResponse> bills = billService.findBills(pageable /*, filter criteria */);
        return ResponseEntity.ok(bills);
    }

    // PUT /api/admin/bills/{id} - US021 (Edit)
    @PutMapping("/bills/{id}")
    public ResponseEntity<BillResponse> updateBill(@PathVariable Long id, @Valid @RequestBody BillRequest billRequest) {
         // Note: Using BillRequest for update might be okay if fields are same
         // Or create a dedicated BillUpdateRequest if needed
        BillResponse updatedBill = billService.updateBill(id, billRequest);
        return ResponseEntity.ok(updatedBill);
    }

     // Maybe add endpoints for specific status changes?
     // e.g., PUT /api/admin/bills/{id}/mark-paid
     // e.g., PUT /api/admin/bills/{id}/void

}
