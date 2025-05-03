package com.hotel.admin.app.controller;

import com.hotel.admin.app.dto.auth.LoginRequest;
// Remove JwtResponse import
// import com.hotel.admin.app.dto.auth.JwtResponse;
import com.hotel.admin.app.dto.auth.UserInfoResponse; // Import new DTO
// Remove JwtUtils import
// import com.hotel.admin.app.security.jwt.JwtUtils;
import com.hotel.admin.app.security.services.UserDetailsImpl;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
// Add Logout import if needed for manual logout handling (though Spring Security handles it)
import org.springframework.security.web.authentication.logout.SecurityContextLogoutHandler;


import org.springframework.web.bind.annotation.*;

import jakarta.servlet.http.HttpServletRequest; // Import for logout
import jakarta.servlet.http.HttpServletResponse;


import java.util.List;
import java.util.stream.Collectors;


@RestController
@RequestMapping("/api/auth")
public class AuthController {

    @Autowired
    AuthenticationManager authenticationManager;

    // Remove JwtUtils autowire
    // @Autowired
    // JwtUtils jwtUtils;

     // Optional: For manual logout triggering if needed
     SecurityContextLogoutHandler logoutHandler = new SecurityContextLogoutHandler();


    @GetMapping("/me")
    public ResponseEntity<?> getCurrentUser() {
         Authentication authentication = SecurityContextHolder.getContext().getAuthentication();

         // Check if the principal is UserDetailsImpl and is actually authenticated (not anonymous user)
         if (authentication != null && authentication.getPrincipal() instanceof UserDetailsImpl) {
             UserDetailsImpl userDetails = (UserDetailsImpl) authentication.getPrincipal();

             List<String> roles = userDetails.getAuthorities().stream()
                 .map(item -> item.getAuthority())
                 .collect(Collectors.toList());

             return ResponseEntity.ok(new UserInfoResponse(
                                       userDetails.getId(),
                                       userDetails.getUsername(),
                                       userDetails.getEmail(),
                                       roles,false
                                    ));

         }
         // If not authenticated (e.g., session expired or cookie missing),
         // Spring Security's filter chain will typically return 401 automatically.
         // This fallback is less likely to be hit for protected endpoints.
         return ResponseEntity.status(401).body("User not authenticated or session expired");
    }



}