package com.example.teamtodo.api.dto;

import com.example.teamtodo.domain.Task;
import com.example.teamtodo.domain.TaskStatus;

import java.time.LocalDate;
import java.time.LocalDateTime;

public record TaskResponse(
    Long id,
    String title,
    LocalDate startDate,
    LocalDate deadline,
    CategorySummary category,
    UserSummary assignee,
    UserSummary createdBy,
    TaskStatus status,
    int progress,
    Long version,
    String legacyAssignee,
    LocalDateTime createdAt,
    LocalDateTime updatedAt
) {
  public static TaskResponse from(Task task) {
    return new TaskResponse(task.getId(), task.getTitle(), task.getStartDate(), task.getDeadline(),
        task.getCategory() == null ? null : new CategorySummary(task.getCategory().getId(), task.getCategory().getName(), task.getCategory().getColor()),
        task.getAssignee() == null ? null : new UserSummary(task.getAssignee().getId(), task.getAssignee().getUsername(), task.getAssignee().getDisplayName()),
        task.getCreatedBy() == null ? null : new UserSummary(task.getCreatedBy().getId(), task.getCreatedBy().getUsername(), task.getCreatedBy().getDisplayName()),
        task.getStatus(), task.getProgress(), task.getVersion(), task.getLegacyAssignee(), task.getCreatedAt(), task.getUpdatedAt());
  }

  public record CategorySummary(Long id, String name, String color) {}
  public record UserSummary(Long id, String username, String displayName) {}
}
