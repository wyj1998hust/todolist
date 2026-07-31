package com.example.teamtodo.domain;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonValue;

import java.util.Locale;

public enum UserRole {
  ADMIN,
  MEMBER;

  @JsonCreator
  public static UserRole fromValue(String value) {
    return value == null ? null : UserRole.valueOf(value.trim().toUpperCase(Locale.ROOT));
  }

  @JsonValue
  public String toValue() {
    return name().toLowerCase(Locale.ROOT);
  }
}
