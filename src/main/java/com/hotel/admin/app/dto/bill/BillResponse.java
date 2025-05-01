package com.hotel.admin.app.dto.bill;


import com.hotel.admin.app.dto.user.UserBasicInfoResponse; // Reuse user DTO
import com.hotel.admin.app.entity.enums.PaymentStatus;

import lombok.Getter;
import lombok.Setter;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

@Getter @Setter
public class BillResponse {
    private Long id;
    private String billId;
    private UserBasicInfoResponse user;
    private LocalDate dateOfIssue;
    private BigDecimal totalAmount;
    private PaymentStatus paymentStatus;
    private List<ChargeResponse> charges; // Include details of charges
}