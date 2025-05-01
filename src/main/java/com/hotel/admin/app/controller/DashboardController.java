package com.hotel.admin.app.controller;


import com.hotel.admin.app.dto.dashboard.DashboardOverviewResponse;
import com.hotel.admin.app.service.DashboardService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/admin/dashboard") 
@RequiredArgsConstructor
@PreAuthorize("hasRole('ADMIN')") 
public class DashboardController {

    private final DashboardService dashboardService;

    
    @GetMapping("/overview")
    public ResponseEntity<DashboardOverviewResponse> getDashboardOverview() {
        DashboardOverviewResponse overview = dashboardService.getDashboardOverview();
        return ResponseEntity.ok(overview);
    }

   
}