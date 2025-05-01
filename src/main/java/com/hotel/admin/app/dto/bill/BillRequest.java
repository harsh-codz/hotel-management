package com.hotel.admin.app.dto.bill;


import jakarta.validation.Valid;
import jakarta.validation.constraints.*;
import lombok.Getter;
import lombok.Setter;
import java.time.LocalDate;
import java.util.List;

import com.hotel.admin.app.entity.enums.PaymentStatus;

@Getter @Setter
public class BillRequest {
    @NotNull(message = "User ID (Customer) is mandatory")
    private Long userId;

    @NotNull(message = "Date of issue is mandatory")
    private LocalDate dateOfIssue;

    // Status often set initially (e.g., PENDING), update via separate endpoint?
    @NotNull(message = "Payment status is mandatory")
    private PaymentStatus paymentStatus = PaymentStatus.PENDING; // Default status

    @NotEmpty(message = "Bill must have at least one charge item")
    @Valid // Validate the nested charge items
    private List<ChargeRequest> charges;

    // totalAmount is calculated by the service
}