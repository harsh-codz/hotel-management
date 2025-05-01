package com.hotel.admin.app.repository;

import com.hotel.admin.app.entity.RoomType;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface RoomTypeRepository extends JpaRepository<RoomType, Long> {
    // Basic CRUD is usually sufficient
}