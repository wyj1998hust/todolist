package com.example.teamtodo.service;

import com.example.teamtodo.api.dto.TaskCreateRequest;
import com.example.teamtodo.api.dto.TaskResponse;
import com.example.teamtodo.api.dto.TaskUpdateRequest;
import com.example.teamtodo.domain.Task;
import com.example.teamtodo.domain.TaskCategory;
import com.example.teamtodo.domain.TaskStatus;
import com.example.teamtodo.domain.UserAccount;
import com.example.teamtodo.exception.AppException;
import com.example.teamtodo.repository.TaskCategoryRepository;
import com.example.teamtodo.repository.TaskRepository;
import com.example.teamtodo.repository.UserAccountRepository;
import com.example.teamtodo.security.AuthenticatedUser;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.List;

@Service
public class TaskService {
  private final TaskRepository taskRepository;
  private final TaskCategoryRepository categoryRepository;
  private final UserAccountRepository userRepository;

  public TaskService(TaskRepository taskRepository, TaskCategoryRepository categoryRepository,
                     UserAccountRepository userRepository) {
    this.taskRepository = taskRepository;
    this.categoryRepository = categoryRepository;
    this.userRepository = userRepository;
  }

  @Transactional(readOnly = true)
  public List<TaskResponse> list(Long categoryId, Long assigneeId, TaskStatus status, LocalDate from, LocalDate to) {
    if (from != null && to != null && from.isAfter(to)) {
      throw new AppException(HttpStatus.BAD_REQUEST, "INVALID_DATE_RANGE", "开始筛选日期不能晚于结束筛选日期");
    }
    return taskRepository.findAll(TaskSpecifications.byFilters(categoryId, assigneeId, status, from, to),
            PageRequest.of(0, 500, Sort.by("startDate").ascending().and(Sort.by("deadline").ascending()).and(Sort.by("id").ascending())))
        .getContent().stream().map(TaskResponse::from).toList();
  }

  @Transactional
  public TaskResponse create(TaskCreateRequest request, AuthenticatedUser actor) {
    TaskRules.requireValidDateRange(request.startDate(), request.deadline());
    Task task = new Task();
    task.setTitle(request.title().trim());
    task.setStartDate(request.startDate());
    task.setDeadline(request.deadline());
    task.setCategory(requireActiveCategory(request.categoryId()));
    task.setAssignee(requireActiveUser(request.assigneeId()));
    task.setCreatedBy(requireUser(actor.id()));
    TaskRules.TaskState state = TaskRules.normalize(request.status(), request.progress() == null ? 0 : request.progress());
    task.setStatus(state.status());
    task.setProgress(state.progress());
    return TaskResponse.from(taskRepository.saveAndFlush(task));
  }

  @Transactional
  public TaskResponse update(Long taskId, TaskUpdateRequest request, AuthenticatedUser actor) {
    Task task = requireTask(taskId);
    requireCanEdit(task, actor);
    if (!task.getVersion().equals(request.version())) {
      throw new AppException(HttpStatus.CONFLICT, "TASK_VERSION_CONFLICT", "任务已被其他用户更新，请刷新后重试");
    }
    if (request.title() != null) {
      String title = request.title().trim();
      if (title.isBlank()) {
        throw new AppException(HttpStatus.BAD_REQUEST, "INVALID_TITLE", "任务标题不能为空");
      }
      task.setTitle(title);
    }
    if (request.startDate() != null) {
      task.setStartDate(request.startDate());
    }
    if (request.deadline() != null) {
      task.setDeadline(request.deadline());
    }
    TaskRules.requireValidDateRange(task.getStartDate(), task.getDeadline());
    if (request.categoryId() != null) {
      task.setCategory(requireActiveCategory(request.categoryId()));
    }
    if (request.assigneeId() != null && !request.assigneeId().equals(task.getAssignee() == null ? null : task.getAssignee().getId())) {
      if (!actor.isAdmin()) {
        throw new AppException(HttpStatus.FORBIDDEN, "ASSIGNEE_CHANGE_FORBIDDEN", "普通成员不能重新分配任务");
      }
      task.setAssignee(requireActiveUser(request.assigneeId()));
    }
    int nextProgress = request.progress() == null ? task.getProgress() : request.progress();
    TaskStatus nextStatus = request.status() == null ? task.getStatus() : request.status();
    TaskRules.TaskState state = TaskRules.normalize(nextStatus, nextProgress);
    task.setStatus(state.status());
    task.setProgress(state.progress());
    return TaskResponse.from(taskRepository.saveAndFlush(task));
  }

  @Transactional
  public void delete(Long taskId, AuthenticatedUser actor) {
    if (!actor.isAdmin()) {
      throw new AppException(HttpStatus.FORBIDDEN, "TASK_DELETE_FORBIDDEN", "只有管理员可以删除任务");
    }
    taskRepository.delete(requireTask(taskId));
  }

  private Task requireTask(Long id) {
    return taskRepository.findById(id)
        .orElseThrow(() -> new AppException(HttpStatus.NOT_FOUND, "TASK_NOT_FOUND", "任务不存在"));
  }

  private TaskCategory requireActiveCategory(Long id) {
    return categoryRepository.findById(id).filter(TaskCategory::isActive)
        .orElseThrow(() -> new AppException(HttpStatus.BAD_REQUEST, "INVALID_CATEGORY", "任务分类不存在或已停用"));
  }

  private UserAccount requireActiveUser(Long id) {
    return userRepository.findById(id).filter(UserAccount::isActive)
        .orElseThrow(() -> new AppException(HttpStatus.BAD_REQUEST, "INVALID_ASSIGNEE", "跟进人不存在或已停用"));
  }

  private UserAccount requireUser(Long id) {
    return userRepository.findById(id)
        .orElseThrow(() -> new AppException(HttpStatus.UNAUTHORIZED, "UNAUTHENTICATED", "当前用户不存在"));
  }

  private void requireCanEdit(Task task, AuthenticatedUser actor) {
    if (actor.isAdmin()) {
      return;
    }
    if (task.getAssignee() == null || !task.getAssignee().getId().equals(actor.id())) {
      throw new AppException(HttpStatus.FORBIDDEN, "TASK_EDIT_FORBIDDEN", "只能编辑自己负责的任务");
    }
  }
}
