package com.example.teamtodo.exception;

import com.example.teamtodo.api.dto.ApiError;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.http.ResponseEntity;
import org.springframework.orm.ObjectOptimisticLockingFailureException;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.method.annotation.MethodArgumentTypeMismatchException;

import java.time.Instant;
import java.util.LinkedHashMap;
import java.util.Map;

@RestControllerAdvice
public class GlobalExceptionHandler {
  @ExceptionHandler(AppException.class)
  public ResponseEntity<ApiError> handleAppException(AppException exception) {
    return ResponseEntity.status(exception.getStatus()).body(ApiError.of(exception.getCode(), exception.getMessage()));
  }

  @ExceptionHandler(MethodArgumentNotValidException.class)
  public ResponseEntity<ApiError> handleValidation(MethodArgumentNotValidException exception) {
    Map<String, String> errors = new LinkedHashMap<>();
    for (FieldError error : exception.getBindingResult().getFieldErrors()) {
      errors.putIfAbsent(error.getField(), error.getDefaultMessage());
    }
    return ResponseEntity.badRequest().body(new ApiError("VALIDATION_ERROR", "请求参数不合法", errors, Instant.now()));
  }

  @ExceptionHandler({ObjectOptimisticLockingFailureException.class, jakarta.persistence.OptimisticLockException.class})
  public ResponseEntity<ApiError> handleOptimisticLock(Exception exception) {
    return ResponseEntity.status(409).body(ApiError.of("TASK_VERSION_CONFLICT", "任务已被其他用户更新，请刷新后重试"));
  }

  @ExceptionHandler(DataIntegrityViolationException.class)
  public ResponseEntity<ApiError> handleDataIntegrity(DataIntegrityViolationException exception) {
    return ResponseEntity.status(409).body(ApiError.of("DATA_CONFLICT", "数据已存在或关联数据无效"));
  }

  @ExceptionHandler({MethodArgumentTypeMismatchException.class, IllegalArgumentException.class})
  public ResponseEntity<ApiError> handleTypeMismatch(Exception exception) {
    return ResponseEntity.badRequest().body(ApiError.of("VALIDATION_ERROR", "请求参数格式不正确"));
  }

  @ExceptionHandler(Exception.class)
  public ResponseEntity<ApiError> handleUnexpected(Exception exception) {
    return ResponseEntity.internalServerError().body(ApiError.of("INTERNAL_ERROR", "服务器暂时无法处理请求"));
  }
}
