package com.example.teamtodo.service;

import com.example.teamtodo.domain.TaskStatus;
import com.example.teamtodo.exception.AppException;
import org.springframework.http.HttpStatus;

import java.time.LocalDate;

public final class TaskRules {
  private TaskRules() {}

  public static TaskState normalize(TaskStatus requestedStatus, int requestedProgress) {
    if (requestedProgress < 0 || requestedProgress > 100) {
      throw new AppException(HttpStatus.BAD_REQUEST, "INVALID_PROGRESS", "任务进度必须在0到100之间");
    }
    TaskStatus status = requestedStatus;
    if (status == null) {
      status = requestedProgress == 100 ? TaskStatus.COMPLETED
          : requestedProgress > 0 ? TaskStatus.IN_PROGRESS : TaskStatus.NOT_STARTED;
    }
    int progress = requestedProgress;
    if (status == TaskStatus.COMPLETED || progress == 100) {
      status = TaskStatus.COMPLETED;
      progress = 100;
    } else if (status == TaskStatus.NOT_STARTED && progress > 0) {
      status = TaskStatus.IN_PROGRESS;
    }
    return new TaskState(status, progress);
  }

  public static void requireValidDateRange(LocalDate startDate, LocalDate deadline) {
    if (startDate == null || deadline == null || startDate.isAfter(deadline)) {
      throw new AppException(HttpStatus.BAD_REQUEST, "INVALID_DATE_RANGE", "开始日期不能晚于截止日期");
    }
  }

  public record TaskState(TaskStatus status, int progress) {}
}
