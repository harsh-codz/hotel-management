package com.hotel.admin.app.repository;

import com.hotel.admin.app.entity.User;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository // Indicates this is a repository
public interface UserRepository extends JpaRepository<User, Long> ,UserRepositoryCustom{
    // Custom method to find a user by username (Spring Data JPA automatically implements this)
    Optional<User> findByUsername(String username);

    // Optional: Check if username or email exists (useful during registration/creation)
    Boolean existsByUsername(String username);
    Boolean existsByEmail(String email);
    Boolean existsByEmailAndIdNot(String email, Long id);
    @Query("SELECT COUNT(u) FROM User u JOIN u.roles r WHERE r.name = :roleName")
long countByRoleName(@Param("roleName") String roleName);

    Page<User> findAll(Specification<User> spec, Pageable pageable);
}