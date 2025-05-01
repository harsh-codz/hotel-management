package com.hotel.admin.app.entity;


import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.ToString;
import java.util.Objects;

@Entity
@Table(name = "complaint_category")
@Getter @Setter @ToString @NoArgsConstructor
public class ComplaintCategory {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, unique = true, length = 100)
    private String name; // e.g., "Maintenance", "Billing", "Service", "Housekeeping"

    public ComplaintCategory(String name) { this.name = name; }

    @Override
    public boolean equals(Object o) { /* ID based equals */
         if (this == o) return true;
         if (o == null || getClass() != o.getClass()) return false;
         ComplaintCategory that = (ComplaintCategory) o;
         return id != null && id.equals(that.id);
    }
    @Override
    public int hashCode() { /* ID based hashCode */
         return id != null ? Objects.hash(id) : super.hashCode();
    }
}