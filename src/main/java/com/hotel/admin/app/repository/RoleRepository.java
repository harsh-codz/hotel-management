package com.hotel.admin.app.repository;

import com.hotel.admin.app.entity.Role;
import com.hotel.admin.app.entity.Role.ERole;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;
import java.util.Set;

@Repository
public interface RoleRepository extends JpaRepository<Role, Long> {
    Optional<Role> findByName(ERole name);
    Set<Role> findByNameIn(Set<ERole> names);
}