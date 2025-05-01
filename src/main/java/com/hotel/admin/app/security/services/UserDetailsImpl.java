package com.hotel.admin.app.security.services;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.hotel.admin.app.entity.User;
import com.hotel.admin.app.entity.enums.UserStatus;

import lombok.Data;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;

import java.util.Collection;
import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;

@Data // Lombok: generates getters, setters, toString, equals, hashCode
public class UserDetailsImpl implements UserDetails {
    private static final long serialVersionUID = 1L;

    private Long id;

    private String username;

    private String email;

    @JsonIgnore // Exclude from JSON serialization (important for security)
    private String password;

    private Collection<? extends GrantedAuthority> authorities;

    public UserDetailsImpl(Long id, String username, String email, String password,
    Collection<? extends GrantedAuthority> authorities, UserStatus userStatus) {
this.id = id;
this.username = username;
this.email = email;
this.password = password;
this.authorities = authorities;
this.userStatusFromUserEntity = userStatus; // Set the status
}

    // Static factory method to build UserDetailsImpl from User entity
    public static UserDetailsImpl build(User user) {
        List<GrantedAuthority> authorities = user.getRoles().stream()
                .map(role -> new SimpleGrantedAuthority(role.getName().name()))
                .collect(Collectors.toList());

        return new UserDetailsImpl(
                user.getId(),
                user.getUsername(),
                user.getEmail(),
                user.getPassword(),
                authorities,
                user.getStatus()); // Pass the status
    }
    // --- UserDetails interface methods ---
    @Override
    public Collection<? extends GrantedAuthority> getAuthorities() {
        return authorities;
    }

    @Override
    public String getPassword() {
        return password;
    }

    @Override
    public String getUsername() {
        return username;
    }

    @Override
    public boolean isAccountNonExpired() {
        return true; // Simple implementation, assume never expired
    }

    @Override
    public boolean isAccountNonLocked() {
        return true; // Simple implementation, assume never locked
    }

    @Override
    public boolean isCredentialsNonExpired() {
         // Could potentially check passwordResetRequired flag here if desired
        return true; // Simple implementation, assume never expired
    }

    @Override
    public boolean isEnabled() {
        return UserStatus.ACTIVE.equals(this.userStatusFromUserEntity);
    }

    // Corrected isEnabled based on User entity status
    private UserStatus userStatusFromUserEntity; // Add this field



    // --- Equals and hashCode for comparison ---
    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        UserDetailsImpl that = (UserDetailsImpl) o;
        return Objects.equals(id, that.id); // Compare based on ID
    }

    @Override
    public int hashCode() {
        return Objects.hash(id);
    }
}