package com.gridops.auth.dto;

import com.gridops.auth.entity.Role;
import com.gridops.auth.entity.User;

public class UserSummaryDto {

    private Long id;
    private String username;
    private Role role;

    public UserSummaryDto(Long id, String username, Role role) {
        this.id = id;
        this.username = username;
        this.role = role;
    }

    public static UserSummaryDto fromEntity(User user) {
        return new UserSummaryDto(user.getId(), user.getUsername(), user.getRole());
    }

    public Long getId() {
        return id;
    }

    public String getUsername() {
        return username;
    }

    public Role getRole() {
        return role;
    }
}
