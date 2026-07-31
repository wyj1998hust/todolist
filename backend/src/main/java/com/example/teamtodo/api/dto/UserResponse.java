package com.example.teamtodo.api.dto;

import com.example.teamtodo.domain.UserAccount;
import com.example.teamtodo.domain.UserRole;

import java.time.LocalDateTime;

public record UserResponse(
    Long id,
    String username,
    String displayName,
    UserRole role,
    boolean active,
    LocalDateTime createdAt,
    LocalDateTime updatedAt
) {
  public static UserResponse from(UserAccount user) {
    return new UserResponse(user.getId(), user.getUsername(), user.getDisplayName(), user.getRole(),
        user.isActive(), user.getCreatedAt(), user.getUpdatedAt());
  }
}
