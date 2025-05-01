package com.hotel.admin.app.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;

import com.hotel.admin.app.entity.Room;
import com.hotel.admin.app.entity.enums.RoomStatus;



public interface RoomRepository  extends JpaRepository<Room,Long>,JpaSpecificationExecutor<Room>{
    long countByRoomStatus(RoomStatus status);
    boolean existsByRoomNumber(String roomNumber);
     boolean existsByRoomNumberAndIdNot(String roomNumber,Long id);
}
