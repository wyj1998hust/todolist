package com.example.teamtodo.domain;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonValue;

import java.util.Locale;

public enum TaskStatus {
  NOT_STARTED,
  IN_PROGRESS,
  COMPLETED;

  @JsonCreator
  public static TaskStatus fromValue(String value) {
    return value == null ? null : TaskStatus.valueOf(value.trim().toUpperCase(Locale.ROOT));
  }

  @JsonValue
  public String toValue() {
    return name().toLowerCase(Locale.ROOT);
  }
}
