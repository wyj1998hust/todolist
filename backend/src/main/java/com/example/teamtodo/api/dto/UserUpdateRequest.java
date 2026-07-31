package com.example.teamtodo.api.dto;

import com.example.teamtodo.domain.UserRole;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;

public record UserUpdateRequest(
    @Size(max = 100, message = "姓名不能超过100个字符") String displayName,
    @Size(min = 8, max = 100, message = "密码长度必须为8到100个字符") String password,
    UserRole role,
    Boolean active
) {}
