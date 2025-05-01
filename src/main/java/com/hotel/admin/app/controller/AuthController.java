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


    // @PostMapping("/login") // US014 Access endpoint
    // public ResponseEntity<?> authenticateUser(@Valid @RequestBody LoginRequest loginRequest, HttpServletRequest request) {

    //     // Authenticate using Spring Security's AuthenticationManager
    //     // If authentication is successful, Spring Security automatically creates a session
    //     // and sets the SecurityContext. A JSESSIONID cookie is sent in the response.
    //     Authentication authentication = authenticationManager.authenticate(
    //             new UsernamePasswordAuthenticationToken(loginRequest.getUsername(), loginRequest.getPassword()));

    //     // SecurityContextHolder.getContext().setAuthentication(authentication); // This is often done automatically by AuthenticationManager if session management is not STATELESS

    //     // Get authenticated user details
    //     UserDetailsImpl userDetails = (UserDetailsImpl) authentication.getPrincipal();

    //     // Get user roles
    //     List<String> roles = userDetails.getAuthorities().stream()
    //             .map(item -> item.getAuthority())
    //             .collect(Collectors.toList());

    //     // Return user info
    //     // NOTE: No JWT token is returned here. The client relies on the JSESSIONID cookie.
    //     return ResponseEntity.ok(new UserInfoResponse(
    //                                     userDetails.getId(),
    //                                     userDetails.getUsername(),
    //                                     userDetails.getEmail(),
    //                                     roles,
    //                                     false)); // add a method to work is reset required
    // }


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

    // Logout Endpoint - Spring Security handles the core logic at /api/auth/logout (defined in SecurityConfig)
    // This controller endpoint is just for consistency and optional custom handling/response.
    // @PostMapping("/logout")
    //  public ResponseEntity<?> logoutUser(HttpServletRequest request, HttpServletResponse response) {
    //      // The logout filter configured in SecurityConfig does most of the work (invalidating session, clearing security context)
    //      // We can optionally call the handler directly or just return a success message.
    //      Authentication auth = SecurityContextHolder.getContext().getAuthentication();
    //      if (auth != null){
    //          logoutHandler.logout(request, response, auth); // Invalidate session, clear context
    //      }
    //      // Spring Security's success handler will likely already set the status, but we can confirm
    //      return ResponseEntity.ok().body("Logout successful");
    //  }

}