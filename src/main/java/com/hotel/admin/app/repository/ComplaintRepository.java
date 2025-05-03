package com.hotel.admin.app.repository;

import com.hotel.admin.app.entity.Complaint;
import com.hotel.admin.app.entity.enums.ComplaintStatus;
import com.hotel.admin.app.entity.enums.RoomStatus;

import java.util.List;

import org.springframework.data.domain.Page; // Import Page
import org.springframework.data.domain.Pageable; // Import Pageable
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor; // For filtering
import org.springframework.stereotype.Repository;

@Repository
public interface ComplaintRepository extends JpaRepository<Complaint, Long>, JpaSpecificationExecutor<Complaint> {

    // Method for Staff view (filtering by assignedStaff ID)
    Page<Complaint> findByAssignedStaffId(Long staffId, Pageable pageable);
  
    long countByStatusIn(List<ComplaintStatus> asList);

     // Add other specific finders if needed without Specifications
     // Page<Complaint> findByStatusAndAssignedStaffId(ComplaintStatus status, Long staffId, Pageable pageable);
}