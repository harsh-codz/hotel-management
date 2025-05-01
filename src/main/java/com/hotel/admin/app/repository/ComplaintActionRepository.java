package com.hotel.admin.app.repository;


import com.hotel.admin.app.entity.ComplaintAction;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface ComplaintActionRepository extends JpaRepository<ComplaintAction, Long> {
    // Basic CRUD usually sufficient
}