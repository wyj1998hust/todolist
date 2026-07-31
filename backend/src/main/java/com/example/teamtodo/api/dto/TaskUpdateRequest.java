package com.example.teamtodo.api.dto;

import com.example.teamtodo.domain.TaskStatus;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

import java.time.LocalDate;

public record TaskUpdateRequest(
    @Size(min = 1, max = 255, message = "任务标题长度必须为1到255个字符") String title,
    LocalDate startDate,
    LocalDate deadline,
    Long categoryId,
    Long assigneeId,
    @Min(value = 0, message = "进度不能小于0") @Max(value = 100, message = "进度不能大于100") Integer progress,
    TaskStatus status,
    @NotNull(message = "任务版本不能为空") Long version
) {}
