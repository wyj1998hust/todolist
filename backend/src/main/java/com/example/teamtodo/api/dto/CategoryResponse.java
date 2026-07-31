package com.example.teamtodo.api.dto;

import com.example.teamtodo.domain.TaskCategory;

import java.time.LocalDateTime;

public record CategoryResponse(
    Long id,
    String name,
    String color,
    int sortOrder,
    boolean active,
    LocalDateTime createdAt,
    LocalDateTime updatedAt
) {
  public static CategoryResponse from(TaskCategory category) {
    return new CategoryResponse(category.getId(), category.getName(), category.getColor(), category.getSortOrder(),
        category.isActive(), category.getCreatedAt(), category.getUpdatedAt());
  }
}
