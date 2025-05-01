package com.hotel.admin.app.initializr;

import com.hotel.admin.app.entity.Role;
import com.hotel.admin.app.entity.User;
import com.hotel.admin.app.repository.RoleRepository;
import com.hotel.admin.app.repository.UserRepository;
import com.hotel.admin.app.entity.Role.ERole;
import com.hotel.admin.app.entity.enums.UserStatus;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.CommandLineRunner;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;

import java.util.HashSet;
import java.util.Optional;
import java.util.Set;

@Component // Make this a Spring component
public class DataInitializer implements CommandLineRunner {

    private static final Logger logger = LoggerFactory.getLogger(DataInitializer.class);

    @Autowired
    UserRepository userRepository;

    @Autowired
    RoleRepository roleRepository; // Need a RoleRepository

    @Autowired
    PasswordEncoder encoder; // Need the password encoder

    // Need a RoleRepository
    // Create RoleRepository.java in com.hotel.admin.app.repository
    /*
    package com.hotel.admin.app.repository;

    import com.hotel.admin.app.entity.Role;
    import com.hotel.admin.app.entity.Role.ERole;
    import org.springframework.data.jpa.repository.JpaRepository;
    import org.springframework.stereotype.Repository;

    import java.util.Optional;

    @Repository
    public interface RoleRepository extends JpaRepository<Role, Long> {
        Optional<Role> findByName(ERole name);
    }
     */

    @Override
    public void run(String... args) throws Exception {
        // Check if any users exist. If not, create an initial admin user.
        if (userRepository.count() == 0) {
            logger.info("No users found. Creating initial ADMIN user.");

            Optional<Role> adminRoleOpt = roleRepository.findByName(ERole.ROLE_ADMIN);
            if (!adminRoleOpt.isPresent()) {
                logger.error("ROLE_ADMIN not found in database. Cannot create initial admin user.");
                return; // Cannot proceed without the Admin role
            }
            Role adminRole = adminRoleOpt.get();

            User adminUser = new User("admin", "admin@example.com", encoder.encode("password123")); // Use a strong default password!
            adminUser.setFullName("Super Administrator");
            adminUser.setStatus(UserStatus.ACTIVE);
            adminUser.setPasswordResetRequired(true); // Require password change on first login

            Set<Role> roles = new HashSet<>();
            roles.add(adminRole);
            adminUser.setRoles(roles);

            userRepository.save(adminUser);
            logger.info("Initial ADMIN user created with username 'admin' and password 'password123'. Password change required on first login.");
        } else {
             logger.info("Existing users found. Skipping initial ADMIN user creation.");
        }
    }
}