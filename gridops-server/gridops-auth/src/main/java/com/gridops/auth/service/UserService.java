package com.gridops.auth.service;

import com.gridops.auth.dto.UserSummaryDto;
import com.gridops.auth.entity.Role;
import com.gridops.auth.repository.UserRepository;

import jakarta.persistence.EntityNotFoundException;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@Transactional(readOnly = true)
public class UserService {

    private final UserRepository userRepository;

    public UserService(UserRepository userRepository) {
        this.userRepository = userRepository;
    }

    public UserSummaryDto findById(Long id) {
        return userRepository.findById(id)
                .map(UserSummaryDto::fromEntity)
                .orElseThrow(() -> new EntityNotFoundException("User not found with id: " + id));
    }

    public UserSummaryDto findByUsername(String username) {
        return userRepository.findByUsername(username)
                .map(UserSummaryDto::fromEntity)
                .orElseThrow(() -> new EntityNotFoundException("User not found: " + username));
    }

    public List<UserSummaryDto> findEngineers() {
        return userRepository.findByRoleAndActiveTrue(Role.ENGINEER).stream()
                .map(UserSummaryDto::fromEntity)
                .toList();
    }
}
