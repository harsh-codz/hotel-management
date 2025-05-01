package com.hotel.admin.app.entity;


import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.ToString;

import java.math.BigDecimal;
import java.util.Objects;

import com.hotel.admin.app.entity.enums.ChargeType;

@Entity
@Table(name = "charge")
@Getter
@Setter
@ToString(exclude = {"bill", "service"})
@NoArgsConstructor
public class Charge {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "bill_id", nullable = false) // Charge must belong to a bill
    private Bill bill;

    @Column(nullable = false, length = 255) // US021
    private String description;

    @Enumerated(EnumType.STRING)
    @Column(length = 20, nullable = false) // US021
    private ChargeType chargeType; // ROOM, SERVICE, ADDITIONAL, TAX, DISCOUNT

    // Optional: Quantity and Unit Price - calculate amount from these if present
    @Column
    private Integer quantity = 1; // Default to 1 if not provided

    @Column(precision = 10, scale = 2)
    private BigDecimal unitPrice; // Price per item/service

    // Mandatory: The final amount for this charge line item.
    // Can be calculated (qty * unitPrice) or entered directly.
    @Column(nullable = false, precision = 10, scale = 2) // US021
    private BigDecimal amount;

    // Optional: Link to a predefined Service if chargeType is SERVICE
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "service_id", nullable = true)
    private Service service;

    // Method to calculate amount if quantity and unit price are set
    public void calculateAmount() {
        if (this.quantity != null && this.unitPrice != null) {
            this.amount = this.unitPrice.multiply(BigDecimal.valueOf(this.quantity));
        }
        // If amount is set directly, this method doesn't override it unless called explicitly
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Charge charge = (Charge) o;
        return id != null && id.equals(charge.id);
    }

    @Override
    public int hashCode() {
        return id != null ? Objects.hash(id) : super.hashCode();
    }
}