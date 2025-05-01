package com.hotel.admin.app.entity;
import jakarta.persistence.*;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;

@Entity
@Table(name = "role") // Table name
@Data // Lombok: generates getters, setters, toString, equals, hashCode
@NoArgsConstructor // Lombok: generates no-argument constructor
@AllArgsConstructor // Lombok: generates constructor with all fields
public class Role {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY) // Auto-incrementing primary key
    private Long id;

    @Enumerated(EnumType.STRING) // Store Enum name as String
    @Column(length = 20, unique = true) // Limit string length, ensure uniqueness
    private ERole name; // Use an Enum for role names

    // Enum for roles
    public enum ERole {
        ROLE_ADMIN,
        ROLE_STAFF,
        ROLE_CUSTOMER
    }
}