package com.example.teamtodo.api.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record ChangePasswordRequest(
    @NotBlank(message = "当前密码不能为空") String currentPassword,
    @NotBlank(message = "新密码不能为空")
    @Size(min = 8, max = 100, message = "密码长度必须为8到100个字符") String newPassword
) {}
