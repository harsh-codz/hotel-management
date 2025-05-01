package com.hotel.admin.app.service;

import com.hotel.admin.app.dto.bill.*;
import com.hotel.admin.app.dto.user.UserBasicInfoResponse;
import com.hotel.admin.app.exception.BadRequestException;
import com.hotel.admin.app.exception.ConflictException;
import com.hotel.admin.app.exception.ResourceNotFoundException;
import com.hotel.admin.app.entity.*; // Entities
import com.hotel.admin.app.entity.enums.ChargeType;
import com.hotel.admin.app.entity.enums.PaymentStatus;
import com.hotel.admin.app.repository.*; // Repositories
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Component; // Changed to @Component or @Service
import org.springframework.transaction.annotation.Transactional;


import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

@Component // Or @Service
@RequiredArgsConstructor
public class BillService {

    private static final Logger log = LoggerFactory.getLogger(BillService.class);

    private final BillRepository billRepository;
// Needed if manipulating charges separately
    private final ServiceRepository serviceRepository;
    private final UserRepository userRepository;
    private final UserInfoService userInfoService; // Assume a service to get UserBasicInfoResponse


    // US021: List available services
    public List<ServiceResponse> getAllServices() {
        return serviceRepository.findAll().stream()
                .map(this::mapToServiceResponse)
                .collect(Collectors.toList());
    }

    // US020, US021: Create Bill
    @Transactional
    public BillResponse createBill(BillRequest request) {
        log.info("Attempting to create bill for user ID {}", request.getUserId());

        User user = userRepository.findById(request.getUserId())
                .orElseThrow(() -> new BadRequestException("Invalid User ID: " + request.getUserId()));

        Bill bill = new Bill();
        bill.setUser(user);
        bill.setDateOfIssue(request.getDateOfIssue());
        bill.setPaymentStatus(request.getPaymentStatus()); // Use provided status or default
        bill.setBillId(generateBillId()); // Generate unique ID

        // Process and add charges
        for (ChargeRequest chargeReq : request.getCharges()) {
            Charge charge = mapChargeRequestToEntity(chargeReq);
            // Important: Link charge to the bill *before* saving bill (due to CascadeType.ALL)
            bill.addCharge(charge); // This also calls recalculateTotalAmount
        }

        // Total amount is now calculated by addCharge -> recalculateTotalAmount
        // bill.recalculateTotalAmount(); // Ensure it's calculated if addCharge doesn't

        Bill savedBill = billRepository.save(bill);
        log.info("Successfully created bill ID: {}", savedBill.getId());
        return mapToBillResponse(savedBill);
    }


    // US020: Get Bill by ID
    public BillResponse getBillById(Long id) {
        Bill bill = billRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Bill", "id", id));
        // Ensure charges are loaded if fetch type is LAZY (should be handled by transaction or OpenSessionInView)
        return mapToBillResponse(bill);
    }


    // US020: List/Search Bills (Requires dynamic query in repository or Specification)
    public Page<BillResponse> findBills(Pageable pageable /* Add filter params here */) {
        // TODO: Implement filtering based on params (date range, customer, status, amount)
        // Example: Call a custom repository method like billRepository.findWithFilters(...)
        Page<Bill> billPage = billRepository.findAll(pageable); // Placeholder - needs filtering
        return billPage.map(this::mapToBillResponse);
    }


    // US021: Edit Bill (More complex - involves adding/removing/updating charges)
    @Transactional
    public BillResponse updateBill(Long id, BillRequest request) {
        log.info("Attempting to update bill ID: {}", id);
        Bill existingBill = billRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Bill", "id", id));

        // Prevent updates if bill is already PAID or VOID?
        if (existingBill.getPaymentStatus() == PaymentStatus.PAID || existingBill.getPaymentStatus() == PaymentStatus.VOID) {
             throw new ConflictException("Cannot update a bill that is already " + existingBill.getPaymentStatus());
        }

        // Update bill header info
        User user = userRepository.findById(request.getUserId())
                .orElseThrow(() -> new BadRequestException("Invalid User ID: " + request.getUserId()));
        existingBill.setUser(user); // Allow changing user? Maybe not.
        existingBill.setDateOfIssue(request.getDateOfIssue());
        existingBill.setPaymentStatus(request.getPaymentStatus());

        // --- Handle Charge Updates (Example: Replace all charges) ---
        // This is simplistic. A real update might involve finding existing charges,
        // updating them, adding new ones, and deleting removed ones.
        // For simplicity here, we remove old and add new based on the request.
        existingBill.getCharges().clear(); // Remove existing charges (orphanRemoval=true deletes them)
        billRepository.flush(); // Ensure deletes happen before adds if constraints exist

        for (ChargeRequest chargeReq : request.getCharges()) {
            Charge charge = mapChargeRequestToEntity(chargeReq);
            existingBill.addCharge(charge); // Adds new charge and recalculates total
        }
        // --- End Charge Update ---

        Bill updatedBill = billRepository.save(existingBill);
        log.info("Successfully updated bill ID: {}", updatedBill.getId());
        return mapToBillResponse(updatedBill);
    }


    // --- Helper & Mapping Methods ---

    private String generateBillId() {
        return "INV-" + UUID.randomUUID().toString().substring(0, 10).toUpperCase();
    }

    private Charge mapChargeRequestToEntity(ChargeRequest req) {
        Charge charge = new Charge();
        charge.setDescription(req.getDescription());
        charge.setChargeType(req.getChargeType());
        charge.setQuantity(req.getQuantity() != null ? req.getQuantity() : 1); // Default quantity if needed
        charge.setUnitPrice(req.getUnitPrice());
        charge.setAmount(req.getAmount()); // Assume amount is provided or calculated before mapping

        // Explicitly calculate amount if quantity/unit price are given and amount isn't primary
        // if (req.getQuantity() != null && req.getUnitPrice() != null) {
        //     charge.calculateAmount(); // Assumes amount calculation logic is in Charge entity
        // }

         if (charge.getAmount() == null) {
             throw new BadRequestException("Charge amount must be provided or calculable from quantity and unit price.");
         }


        if (req.getServiceId() != null) {
            Service service = serviceRepository.findById(req.getServiceId())
                    .orElseThrow(() -> new BadRequestException("Invalid Service ID: " + req.getServiceId()));
            charge.setService(service);
             // Optionally default description/amount if service ID provided and fields are empty
            if (charge.getDescription() == null || charge.getDescription().isBlank()) {
                charge.setDescription(service.getName());
            }
             if (charge.getAmount() == null && service.getDefaultPrice() != null) {
                 charge.setUnitPrice(service.getDefaultPrice());
                 charge.calculateAmount(); // Calculate amount based on default service price
             }

        } else if (req.getChargeType() == ChargeType.SERVICE) {
            throw new BadRequestException("Service ID must be provided for charges of type SERVICE.");
        }

        return charge;
    }

     private ServiceResponse mapToServiceResponse(Service service) {
        return new ServiceResponse(service.getId(), service.getName(), service.getDefaultPrice());
    }

    private BillResponse mapToBillResponse(Bill bill) {
        BillResponse response = new BillResponse();
        response.setId(bill.getId());
        response.setBillId(bill.getBillId());
        response.setDateOfIssue(bill.getDateOfIssue());
        response.setTotalAmount(bill.getTotalAmount());
        response.setPaymentStatus(bill.getPaymentStatus());

        if (bill.getUser() != null) {
            // Use UserInfoService or map directly
             response.setUser(userInfoService.mapToUserBasicInfoResponse(bill.getUser()));
        }

        if (bill.getCharges() != null) {
            response.setCharges(bill.getCharges().stream()
                    .map(this::mapToChargeResponse)
                    .collect(Collectors.toList()));
        }
        return response;
    }

    private ChargeResponse mapToChargeResponse(Charge charge) {
        ChargeResponse response = new ChargeResponse();
        response.setId(charge.getId());
        response.setDescription(charge.getDescription());
        response.setChargeType(charge.getChargeType());
        response.setQuantity(charge.getQuantity());
        response.setUnitPrice(charge.getUnitPrice());
        response.setAmount(charge.getAmount());
        if (charge.getService() != null) {
            response.setServiceId(charge.getService().getId());
            response.setServiceName(charge.getService().getName()); // Include name for convenience
        }
        return response;
    }

     // Assuming UserInfoService exists for mapping
     // This could be a separate service or just a helper method/class
     @Component // Make this injectable
     public static class UserInfoService {
         public UserBasicInfoResponse mapToUserBasicInfoResponse(User user) {
            if (user == null) return null;
            return new UserBasicInfoResponse(
                    user.getId(),
                    user.getUsername(),
                    user.getFullName(),
                    user.getEmail()
            );
         }
     }
}