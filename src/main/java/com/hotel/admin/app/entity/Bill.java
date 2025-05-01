package com.hotel.admin.app.entity;


import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.ToString;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

import com.hotel.admin.app.entity.enums.PaymentStatus;

@Entity
@Table(name = "bill")
@Getter
@Setter
@ToString(exclude = {"user", "charges"})
@NoArgsConstructor
public class Bill {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    // Optional unique Bill ID (like Booking ID) - can be generated
    @Column(unique = true, length = 20)
    private String billId; // US020 implies ID

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false) // Bill must belong to a user
    private User user;

    @Column(nullable = false) // US020
    private LocalDate dateOfIssue;

    @Column(nullable = false, precision = 12, scale = 2) // US020
    private BigDecimal totalAmount; // Should be calculated based on charges

    @Enumerated(EnumType.STRING)
    @Column(length = 20, nullable = false) // US020
    private PaymentStatus paymentStatus;

    // Bidirectional relationship: Bill owns the charges
    @OneToMany(mappedBy = "bill", cascade = CascadeType.ALL, orphanRemoval = true, fetch = FetchType.LAZY)
    private List<Charge> charges = new ArrayList<>();

    // Convenience method to add charges and maintain relationship
    public void addCharge(Charge charge) {
        charges.add(charge);
        charge.setBill(this);
        // Recalculate total amount whenever charges change
        recalculateTotalAmount();
    }

    public void removeCharge(Charge charge) {
        charges.remove(charge);
        charge.setBill(null);
         recalculateTotalAmount();
    }

    // Method to calculate total amount based on charges
    public void recalculateTotalAmount() {
        this.totalAmount = charges.stream()
                                 .map(Charge::getAmount) // Get the amount from each charge
                                 .reduce(BigDecimal.ZERO, BigDecimal::add); // Sum them up
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Bill bill = (Bill) o;
        return id != null && id.equals(bill.id);
    }

    @Override
    public int hashCode() {
        return id != null ? Objects.hash(id) : super.hashCode();
    }
}