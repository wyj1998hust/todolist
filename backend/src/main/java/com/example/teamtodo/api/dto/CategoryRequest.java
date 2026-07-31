package com.example.teamtodo.api.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;

public record CategoryRequest(
    @NotBlank(message = "分类名称不能为空") @Size(max = 64, message = "分类名称不能超过64个字符") String name,
    @NotBlank(message = "分类颜色不能为空") @Pattern(regexp = "#[0-9a-fA-F]{6}", message = "颜色必须为6位十六进制颜色") String color,
    Integer sortOrder,
    Boolean active
) {}
