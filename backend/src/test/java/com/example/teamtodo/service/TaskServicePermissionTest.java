package com.example.teamtodo.service;

import com.example.teamtodo.api.dto.TaskCreateRequest;
import com.example.teamtodo.api.dto.TaskResponse;
import com.example.teamtodo.api.dto.TaskUpdateRequest;
import com.example.teamtodo.domain.Task;
import com.example.teamtodo.domain.TaskCategory;
import com.example.teamtodo.domain.TaskStatus;
import com.example.teamtodo.domain.UserAccount;
import com.example.teamtodo.domain.UserRole;
import com.example.teamtodo.exception.AppException;
import com.example.teamtodo.repository.TaskCategoryRepository;
import com.example.teamtodo.repository.TaskRepository;
import com.example.teamtodo.repository.UserAccountRepository;
import com.example.teamtodo.security.AuthenticatedUser;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDate;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class TaskServicePermissionTest {
  @Mock
  private TaskRepository taskRepository;
  @Mock
  private TaskCategoryRepository categoryRepository;
  @Mock
  private UserAccountRepository userRepository;

  private TaskService service;

  @BeforeEach
  void setUp() {
    service = new TaskService(taskRepository, categoryRepository, userRepository);
  }

  @Test
  void memberCanCreateTaskOnlyForSelf() {
    UserAccount member = user(2L, "member1");
    when(member.isActive()).thenReturn(true);
    TaskCategory testCategory = category(10L);
    when(categoryRepository.findById(10L)).thenReturn(Optional.of(testCategory));
    when(userRepository.findById(2L)).thenReturn(Optional.of(member));
    when(taskRepository.saveAndFlush(any(Task.class))).thenAnswer(invocation -> invocation.getArgument(0));

    TaskResponse response = service.create(createRequest(2L), memberPrincipal(2L));

    assertEquals(2L, response.assignee().id());
    assertEquals(2L, response.createdBy().id());
  }

  @Test
  void memberCannotCreateTaskForAnotherUser() {
    AppException exception = assertThrows(AppException.class,
        () -> service.create(createRequest(3L), memberPrincipal(2L)));

    assertEquals("TASK_ASSIGNEE_FORBIDDEN", exception.getCode());
    verifyNoInteractions(taskRepository, categoryRepository, userRepository);
  }

  @Test
  void adminCanCreateTaskForAnotherUser() {
    UserAccount admin = user(1L, "admin");
    UserAccount member = user(3L, "member2");
    when(member.isActive()).thenReturn(true);
    TaskCategory testCategory = category(10L);
    when(categoryRepository.findById(10L)).thenReturn(Optional.of(testCategory));
    when(userRepository.findById(1L)).thenReturn(Optional.of(admin));
    when(userRepository.findById(3L)).thenReturn(Optional.of(member));
    when(taskRepository.saveAndFlush(any(Task.class))).thenAnswer(invocation -> invocation.getArgument(0));

    TaskResponse response = service.create(createRequest(3L), new AuthenticatedUser(1L, "admin", "管理员", UserRole.ADMIN, 0));

    assertEquals(3L, response.assignee().id());
    assertEquals(1L, response.createdBy().id());
  }

  @Test
  void memberCannotEditAnotherUsersTask() {
    Task task = new Task();
    task.setAssignee(userIdOnly(3L));
    when(taskRepository.findById(20L)).thenReturn(Optional.of(task));

    AppException exception = assertThrows(AppException.class, () -> service.update(20L,
        new TaskUpdateRequest("updated", null, null, null, null, null, null, 1L), memberPrincipal(2L)));

    assertEquals("TASK_EDIT_FORBIDDEN", exception.getCode());
    verify(taskRepository, never()).saveAndFlush(any());
  }

  @Test
  void memberCannotEditUnassignedTask() {
    Task task = new Task();
    when(taskRepository.findById(21L)).thenReturn(Optional.of(task));

    AppException exception = assertThrows(AppException.class, () -> service.update(21L,
        new TaskUpdateRequest("updated", null, null, null, null, null, null, 1L), memberPrincipal(2L)));

    assertEquals("TASK_EDIT_FORBIDDEN", exception.getCode());
    verify(taskRepository, never()).saveAndFlush(any());
  }

  private TaskCreateRequest createRequest(Long assigneeId) {
    return new TaskCreateRequest("测试任务", LocalDate.of(2026, 8, 1), LocalDate.of(2026, 8, 2),
        10L, assigneeId, 0, TaskStatus.NOT_STARTED);
  }

  private AuthenticatedUser memberPrincipal(Long id) {
    return new AuthenticatedUser(id, "member" + id, "成员 " + id, UserRole.MEMBER, 0);
  }

  private UserAccount user(Long id, String username) {
    UserAccount user = mock(UserAccount.class);
    when(user.getId()).thenReturn(id);
    when(user.getUsername()).thenReturn(username);
    when(user.getDisplayName()).thenReturn(username);
    return user;
  }

  private UserAccount userIdOnly(Long id) {
    UserAccount user = mock(UserAccount.class);
    when(user.getId()).thenReturn(id);
    return user;
  }

  private TaskCategory category(Long id) {
    TaskCategory category = mock(TaskCategory.class);
    when(category.getId()).thenReturn(id);
    when(category.getName()).thenReturn("测试分类");
    when(category.getColor()).thenReturn("#155EEF");
    when(category.isActive()).thenReturn(true);
    return category;
  }
}
