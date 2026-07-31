package com.example.teamtodo.domain;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;

class EnumJsonValueTest {
  @Test
  void acceptsApiStyleTaskStatus() {
    assertEquals(TaskStatus.IN_PROGRESS, TaskStatus.fromValue("in_progress"));
    assertEquals("completed", TaskStatus.COMPLETED.toValue());
  }

  @Test
  void acceptsApiStyleRole() {
    assertEquals(UserRole.ADMIN, UserRole.fromValue("admin"));
  }
}
