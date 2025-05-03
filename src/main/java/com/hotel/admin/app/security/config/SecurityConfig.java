package com.hotel.admin.app.security.config;

// Necessary imports:
import com.fasterxml.jackson.databind.ObjectMapper; // For JSON handling
import jakarta.servlet.http.HttpServletResponse; // For setting response status/content type
import com.hotel.admin.app.dto.auth.UserInfoResponse; // Your DTO for user info
import com.hotel.admin.app.security.services.UserDetailsImpl; // Your UserDetails implementation
import com.hotel.admin.app.security.services.UserDetailsServiceImpl; // Your UserDetailsService

import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.dao.DaoAuthenticationProvider;
import org.springframework.security.config.annotation.authentication.configuration.AuthenticationConfiguration;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.CorsConfigurationSource;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;


@Configuration
@EnableWebSecurity
@EnableMethodSecurity
public class SecurityConfig {

    @Autowired UserDetailsServiceImpl userDetailsService;
    // Inject ObjectMapper (Spring Boot provides one automatically)
    @Autowired ObjectMapper objectMapper;

    @Bean
    public DaoAuthenticationProvider authenticationProvider() {
        DaoAuthenticationProvider authProvider = new DaoAuthenticationProvider();
        authProvider.setUserDetailsService(userDetailsService);
        authProvider.setPasswordEncoder(passwordEncoder());
        return authProvider;
    }

    @Bean
    public AuthenticationManager authenticationManager(AuthenticationConfiguration authConfig) throws Exception {
        return authConfig.getAuthenticationManager();
    }

    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }

    @Bean
    public SecurityFilterChain filterChain(HttpSecurity http) throws Exception {
        http
            .csrf(csrf -> csrf
                .ignoringRequestMatchers("/api/**", "/h2-console/**") // Disable CSRF for API and H2 console
            )
            .cors(cors -> cors.configurationSource(corsConfigurationSource())) // Apply CORS config
            // No explicit sessionManagement needed for default stateful behavior

            .authorizeHttpRequests(auth ->
                auth.requestMatchers("/api/auth/login").permitAll() // Permit access TO the login processing URL
                    .requestMatchers("/api/auth/logout").permitAll() // Permit access TO the logout processing URL
                    .requestMatchers("/h2-console/**").permitAll()
                    .requestMatchers("/swagger-ui.html").permitAll()
                    .requestMatchers("/swagger-ui/**").permitAll()
                    .requestMatchers("/v3/api-docs/**").permitAll()  // Permit H2 console
                    .requestMatchers("/api/admin/**").hasRole("ADMIN") // Restrict admin endpoints
                    .requestMatchers("/api/staff/**").hasAnyRole("ADMIN", "STAFF") // Restrict staff endpoints
                    .anyRequest().authenticated() // Require authentication for everything else
            )
            // ----- Enable and Configure formLogin -----
            .formLogin(form -> form
                .loginProcessingUrl("/api/auth/login") // Spring Security handles POSTs to this URL
                .usernameParameter("username") // Request body field for username
                .passwordParameter("password") // Request body field for password
                .successHandler((request, response, authentication) -> {
                    // SUCCESS: Session created, cookie will be set by framework.
                    // Now, customize the response body:
                    UserDetailsImpl userDetails = (UserDetailsImpl) authentication.getPrincipal();
                    List<String> roles = userDetails.getAuthorities().stream()
                            .map(item -> item.getAuthority())
                            .collect(Collectors.toList());

                    // Ensure this method exists and works in UserDetailsImpl
                    boolean passwordResetRequired = false;

                    UserInfoResponse userInfo = new UserInfoResponse(
                        userDetails.getId(),
                        userDetails.getUsername(),
                        userDetails.getEmail(),
                        roles,
                        passwordResetRequired
                    );

                    response.setStatus(HttpServletResponse.SC_OK);
                    response.setContentType("application/json");
                    response.getWriter().write(objectMapper.writeValueAsString(userInfo)); // Write JSON body
                    response.getWriter().flush();
                })
                .failureHandler((request, response, exception) -> {
                    // FAILURE: Customize the error response
                    response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
                    response.setContentType("application/json");
                    // Simple JSON error message
                    response.getWriter().write("{\"error\": \"Authentication Failed\", \"message\": \"" + exception.getMessage().replace("\"", "'") + "\"}");
                    response.getWriter().flush();
                })
            )
            // -----------------------------------------------------
             .logout(logout -> logout
                .logoutUrl("/api/auth/logout") // URL Spring Security listens to for logout
                .logoutSuccessHandler((request, response, authentication) -> {
                    // Customize logout success response
                    response.setStatus(HttpServletResponse.SC_OK);
                    response.setContentType("application/json");
                    response.getWriter().write("{\"message\": \"Logout successful\"}");
                    response.getWriter().flush();
                })
                .deleteCookies("JSESSIONID") // Ensure cookie is deleted
                .invalidateHttpSession(true) // Ensure session is invalidated
                .clearAuthentication(true) // Ensure security context is cleared
            )
            .headers(headers -> headers.frameOptions(frameOption -> frameOption.sameOrigin())); // For H2 Console

        // Register the provider used by AuthenticationManager
        http.authenticationProvider(authenticationProvider());

        return http.build();
    }

    // CORS Configuration Bean (remains the same)
    @Bean
    CorsConfigurationSource corsConfigurationSource() {
        CorsConfiguration configuration = new CorsConfiguration();
        configuration.setAllowedOrigins(Arrays.asList("http://localhost:4200", "http://127.0.0.1:4200")); // Adjust for your frontend
        configuration.setAllowedMethods(Arrays.asList("GET", "POST", "PUT", "DELETE", "OPTIONS"));
        configuration.setAllowedHeaders(Arrays.asList("Content-Type", "Accept", "Origin", "X-Requested-With", "Cookie"));
        configuration.setAllowCredentials(true);
        configuration.setMaxAge(3600L);
        UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
        source.registerCorsConfiguration("/**", configuration);
        return source;
    }
}