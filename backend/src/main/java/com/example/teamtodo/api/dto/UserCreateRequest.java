package com.example.teamtodo.api.dto;

import com.example.teamtodo.domain.UserRole;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;

public record UserCreateRequest(
    @NotBlank(message = "用户名不能为空")
    @Pattern(regexp = "[A-Za-z0-9_.-]{3,64}", message = "用户名只能使用字母、数字、点、下划线或连字符") String username,
    @NotBlank(message = "姓名不能为空") @Size(max = 100, message = "姓名不能超过100个字符") String displayName,
    @NotBlank(message = "密码不能为空") @Size(min = 8, max = 100, message = "密码长度必须为8到100个字符") String password,
    @NotNull(message = "角色不能为空") UserRole role
) {}
