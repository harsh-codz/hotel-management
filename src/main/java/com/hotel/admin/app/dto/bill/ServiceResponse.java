package com.hotel.admin.app.dto.bill;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import java.math.BigDecimal;

@Getter @Setter @NoArgsConstructor @AllArgsConstructor
public class ServiceResponse {
    private Long id;
    private String name;
    private BigDecimal defaultPrice;
}