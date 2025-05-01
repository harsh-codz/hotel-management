package com.hotel.admin.app.repository;

import com.hotel.admin.app.entity.Amenity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface AmenityRepository extends JpaRepository<Amenity, Long> {
    // Basic CRUD (including findAllById used in service) is sufficient
}