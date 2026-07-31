package com.example.teamtodo.api.dto;

import com.example.teamtodo.domain.TaskStatus;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

import java.time.LocalDate;

public record TaskCreateRequest(
    @NotBlank(message = "任务标题不能为空") @Size(max = 255, message = "任务标题不能超过255个字符") String title,
    @NotNull(message = "开始日期不能为空") LocalDate startDate,
    @NotNull(message = "截止日期不能为空") LocalDate deadline,
    @NotNull(message = "任务分类不能为空") Long categoryId,
    @NotNull(message = "跟进人不能为空") Long assigneeId,
    @Min(value = 0, message = "进度不能小于0") @Max(value = 100, message = "进度不能大于100") Integer progress,
    TaskStatus status
) {}
