package com.example.teamtodo.service;

import com.example.teamtodo.domain.TaskStatus;
import com.example.teamtodo.exception.AppException;
import org.junit.jupiter.api.Test;

import java.time.LocalDate;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

class TaskRulesTest {
  @Test
  void marksFullProgressAsCompleted() {
    TaskRules.TaskState state = TaskRules.normalize(TaskStatus.IN_PROGRESS, 100);
    assertEquals(TaskStatus.COMPLETED, state.status());
    assertEquals(100, state.progress());
  }

  @Test
  void derivesInProgressFromPartialProgress() {
    TaskRules.TaskState state = TaskRules.normalize(TaskStatus.NOT_STARTED, 40);
    assertEquals(TaskStatus.IN_PROGRESS, state.status());
    assertEquals(40, state.progress());
  }

  @Test
  void rejectsInvertedDates() {
    assertThrows(AppException.class, () -> TaskRules.requireValidDateRange(
        LocalDate.of(2026, 8, 2), LocalDate.of(2026, 8, 1)));
  }
}
