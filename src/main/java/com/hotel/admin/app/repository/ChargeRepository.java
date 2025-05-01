package com.hotel.admin.app.repository;
import com.hotel.admin.app.entity.Charge;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface ChargeRepository extends JpaRepository<Charge, Long> {
    // Usually basic CRUD is sufficient
}
