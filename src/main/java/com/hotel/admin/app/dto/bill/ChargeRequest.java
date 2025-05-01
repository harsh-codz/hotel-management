package com.hotel.admin.app.dto.bill;



import jakarta.validation.constraints.*;
import lombok.Getter;
import lombok.Setter;
import java.math.BigDecimal;

import com.hotel.admin.app.entity.enums.ChargeType;

@Getter @Setter
public class ChargeRequest {
    @NotBlank(message = "Charge description is mandatory")
    @Size(max = 255)
    private String description;

    @NotNull(message = "Charge type is mandatory")
    private ChargeType chargeType;

    private Integer quantity; // Optional, defaults to 1 in service if null

    @Digits(integer=10, fraction=2, message = "Unit price format invalid")
    private BigDecimal unitPrice; // Optional

    @NotNull(message = "Charge amount is mandatory") // Amount MUST be provided if quantity/unitPrice are not used for calculation
    @PositiveOrZero // Allow zero amount? Or should be Positive? Depends on use case (e.g., discounts)
    @Digits(integer=10, fraction=2, message = "Amount format invalid")
    private BigDecimal amount;

    private Long serviceId; // Optional: Link to predefined Service
}