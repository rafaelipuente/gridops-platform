package com.gridops.auth.repository;

import com.gridops.auth.entity.Role;
import com.gridops.auth.entity.User;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface UserRepository extends JpaRepository<User, Long> {

    Optional<User> findByUsername(String username);

    Optional<User> findByEmail(String email);

    List<User> findByRoleAndActiveTrue(Role role);

    Page<User> findByActiveTrue(Pageable pageable);
}
