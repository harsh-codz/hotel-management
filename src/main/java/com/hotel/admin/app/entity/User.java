package com.hotel.admin.app.entity;



import jakarta.persistence.*;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;
import com.hotel.admin.app.entity.enums.UserStatus;
import java.util.HashSet;
import java.util.Set;

@Entity
@Table(name = "users", // Table name
       uniqueConstraints = { // Define unique constraints
           @UniqueConstraint(columnNames = "username"),
           @UniqueConstraint(columnNames = "email")
       })
@Data
@NoArgsConstructor
@AllArgsConstructor
public class User {


    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false) // Mandatory (US019)
    private String username;

    @Column(nullable = false) // Mandatory (US019), will store BCrypt hash
    private String password;

    @Column(nullable = false) // Mandatory (US019)
    private String email;

    private String fullName; // Optional initially, can be added later (US019)

    private String contactPhone; // Optional (US019)

    @Enumerated(EnumType.STRING)
    @Column(length = 20, nullable = false)
    private UserStatus status = UserStatus.ACTIVE;// Default status (US019)

   
@ManyToMany(fetch = FetchType.EAGER) // EAGER often useful for roles with security
@JoinTable( name = "user_roles",
            joinColumns = @JoinColumn(name = "user_id"),
            inverseJoinColumns = @JoinColumn(name = "role_id"))
private Set<Role> roles = new HashSet<>();

// Flag for initial password reset requirement
@Column(nullable = false)
private boolean passwordResetRequired = false;

// Getter/Setter or Lombok annotation for passwordResetRequired
public boolean isPasswordResetRequired() {
    return passwordResetRequired;
}

public void setPasswordResetRequired(boolean passwordResetRequired) {
    this.passwordResetRequired = passwordResetRequired;
}
    // Constructor excluding ID for creation
    public User(String username, String email, String password) {
        this.username = username;
        this.email = email;
        this.password = password;
    }
    
}