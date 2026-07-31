package com.example.teamtodo.api;

import com.example.teamtodo.api.dto.UserCreateRequest;
import com.example.teamtodo.api.dto.UserResponse;
import com.example.teamtodo.api.dto.UserUpdateRequest;
import com.example.teamtodo.exception.AppException;
import com.example.teamtodo.security.AuthenticatedUser;
import com.example.teamtodo.service.UserService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/users")
public class UserController {
  private final UserService userService;

  public UserController(UserService userService) {
    this.userService = userService;
  }

  @GetMapping
  public List<UserResponse> list(@RequestParam(defaultValue = "false") boolean includeInactive,
                                 @AuthenticationPrincipal AuthenticatedUser actor) {
    if (includeInactive) {
      requireAdmin(actor);
    }
    return userService.list(includeInactive);
  }

  @PostMapping
  public UserResponse create(@Valid @RequestBody UserCreateRequest request,
                             @AuthenticationPrincipal AuthenticatedUser actor) {
    requireAdmin(actor);
    return userService.create(request);
  }

  @PatchMapping("/{id}")
  public UserResponse update(@PathVariable Long id, @Valid @RequestBody UserUpdateRequest request,
                             @AuthenticationPrincipal AuthenticatedUser actor) {
    requireAdmin(actor);
    return userService.update(id, request);
  }

  private void requireAdmin(AuthenticatedUser actor) {
    if (!actor.isAdmin()) {
      throw new AppException(HttpStatus.FORBIDDEN, "ADMIN_REQUIRED", "只有管理员可以管理用户");
    }
  }
}
