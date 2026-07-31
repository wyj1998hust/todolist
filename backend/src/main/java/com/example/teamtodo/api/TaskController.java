package com.example.teamtodo.api;

import com.example.teamtodo.api.dto.TaskCreateRequest;
import com.example.teamtodo.api.dto.TaskResponse;
import com.example.teamtodo.api.dto.TaskUpdateRequest;
import com.example.teamtodo.domain.TaskStatus;
import com.example.teamtodo.security.AuthenticatedUser;
import com.example.teamtodo.service.TaskService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.util.List;

@RestController
@RequestMapping("/api/tasks")
public class TaskController {
  private final TaskService taskService;

  public TaskController(TaskService taskService) {
    this.taskService = taskService;
  }

  @GetMapping
  public List<TaskResponse> list(@RequestParam(required = false) Long categoryId,
                                 @RequestParam(required = false) Long assigneeId,
                                 @RequestParam(required = false) TaskStatus status,
                                 @RequestParam(required = false) LocalDate from,
                                 @RequestParam(required = false) LocalDate to) {
    return taskService.list(categoryId, assigneeId, status, from, to);
  }

  @PostMapping
  @ResponseStatus(HttpStatus.CREATED)
  public TaskResponse create(@Valid @RequestBody TaskCreateRequest request,
                             @AuthenticationPrincipal AuthenticatedUser actor) {
    return taskService.create(request, actor);
  }

  @PatchMapping("/{id}")
  public TaskResponse update(@PathVariable Long id, @Valid @RequestBody TaskUpdateRequest request,
                             @AuthenticationPrincipal AuthenticatedUser actor) {
    return taskService.update(id, request, actor);
  }

  @DeleteMapping("/{id}")
  @ResponseStatus(HttpStatus.NO_CONTENT)
  public void delete(@PathVariable Long id, @AuthenticationPrincipal AuthenticatedUser actor) {
    taskService.delete(id, actor);
  }
}
