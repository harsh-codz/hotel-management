package com.hotel.admin.app.repository;

import com.hotel.admin.app.entity.User;
import com.hotel.admin.app.entity.enums.UserStatus;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import java.util.Set;

public interface UserRepositoryCustom {
    Page<User> findWithDynamicQuery(
            String username, String email, UserStatus status, Set<String> roleNames,
            Pageable pageable);
}
