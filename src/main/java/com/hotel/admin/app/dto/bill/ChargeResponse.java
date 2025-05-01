package com.hotel.admin.app.dto.bill;



import lombok.Getter;
import lombok.Setter;
import java.math.BigDecimal;
import com.hotel.admin.app.entity.enums.ChargeType;

@Getter @Setter
public class ChargeResponse {
    private Long id;
    private String description;
    private ChargeType chargeType;
    private Integer quantity;
    private BigDecimal unitPrice;
    private BigDecimal amount;
    private Long serviceId; // ID of the linked service, if any
    private String serviceName; // Name of the linked service, if any
}