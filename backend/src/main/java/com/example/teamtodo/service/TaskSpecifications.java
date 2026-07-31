package com.example.teamtodo.service;

import com.example.teamtodo.domain.Task;
import com.example.teamtodo.domain.TaskStatus;
import org.springframework.data.jpa.domain.Specification;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

public final class TaskSpecifications {
  private TaskSpecifications() {}

  public static Specification<Task> byFilters(Long categoryId, Long assigneeId, TaskStatus status,
                                               LocalDate from, LocalDate to) {
    return (root, query, builder) -> {
      List<jakarta.persistence.criteria.Predicate> predicates = new ArrayList<>();
      if (categoryId != null) {
        predicates.add(builder.equal(root.join("category").get("id"), categoryId));
      }
      if (assigneeId != null) {
        predicates.add(builder.equal(root.join("assignee").get("id"), assigneeId));
      }
      if (status != null) {
        predicates.add(builder.equal(root.get("status"), status));
      }
      if (from != null) {
        predicates.add(builder.greaterThanOrEqualTo(root.get("deadline"), from));
      }
      if (to != null) {
        predicates.add(builder.lessThanOrEqualTo(root.get("startDate"), to));
      }
      return builder.and(predicates.toArray(new jakarta.persistence.criteria.Predicate[0]));
    };
  }
}
