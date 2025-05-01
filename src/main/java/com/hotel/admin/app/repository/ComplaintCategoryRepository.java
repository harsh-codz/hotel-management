package com.hotel.admin.app.repository;


import com.hotel.admin.app.entity.ComplaintCategory;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface ComplaintCategoryRepository extends JpaRepository<ComplaintCategory, Long> {
    // Basic CRUD usually sufficient
}
