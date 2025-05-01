package com.hotel.admin.app.repository;
import com.hotel.admin.app.entity.Bill;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
// Import JpaSpecificationExecutor or implement custom repo if complex filtering needed
// import org.springframework.data.jpa.repository.JpaSpecificationExecutor;

@Repository
public interface BillRepository extends JpaRepository<Bill, Long>  {
   
  
}