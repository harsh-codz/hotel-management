package com.hotel.admin.app.repository;

import com.hotel.admin.app.entity.Service; // Corrected import
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface ServiceRepository extends JpaRepository<Service, Long> { // Corrected generic type
    // Usually basic CRUD is sufficient
}