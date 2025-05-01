package com.hotel.admin.app.entity;

import jakarta.persistence.*;
import lombok.Data;

@Entity
@Table(name = "amenity")
@Data
public class Amenity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, unique = true) // Mandatory and unique (US016)
    private String name;
}