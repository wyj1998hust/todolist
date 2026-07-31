package com.example.teamtodo.service;

import com.example.teamtodo.api.dto.UserCreateRequest;
import com.example.teamtodo.api.dto.UserResponse;
import com.example.teamtodo.api.dto.UserUpdateRequest;
import com.example.teamtodo.domain.UserAccount;
import com.example.teamtodo.domain.UserRole;
import com.example.teamtodo.exception.AppException;
import com.example.teamtodo.repository.UserAccountRepository;
import org.springframework.http.HttpStatus;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
public class UserService {
  private final UserAccountRepository userRepository;
  private final PasswordEncoder passwordEncoder;

  public UserService(UserAccountRepository userRepository, PasswordEncoder passwordEncoder) {
    this.userRepository = userRepository;
    this.passwordEncoder = passwordEncoder;
  }

  @Transactional(readOnly = true)
  public List<UserResponse> list(boolean includeInactive) {
    List<UserAccount> users = includeInactive
        ? userRepository.findAllByOrderByDisplayNameAsc()
        : userRepository.findAllByActiveTrueOrderByDisplayNameAsc();
    return users.stream().map(UserResponse::from).toList();
  }

  @Transactional
  public UserResponse create(UserCreateRequest request) {
    String username = request.username().trim();
    if (userRepository.existsByUsernameIgnoreCase(username)) {
      throw new AppException(HttpStatus.CONFLICT, "USERNAME_EXISTS", "用户名已存在");
    }
    UserAccount user = new UserAccount();
    user.setUsername(username);
    user.setDisplayName(request.displayName().trim());
    user.setPasswordHash(passwordEncoder.encode(request.password()));
    user.setRole(request.role());
    user.setActive(true);
    return UserResponse.from(userRepository.saveAndFlush(user));
  }

  @Transactional
  public UserResponse update(Long id, UserUpdateRequest request) {
    UserAccount user = requireUser(id);
    UserRole nextRole = request.role() == null ? user.getRole() : request.role();
    boolean nextActive = request.active() == null ? user.isActive() : request.active();
    if (user.getRole() == UserRole.ADMIN && user.isActive()
        && (nextRole != UserRole.ADMIN || !nextActive)
        && userRepository.countByRoleAndActiveTrue(UserRole.ADMIN) <= 1) {
      throw new AppException(HttpStatus.BAD_REQUEST, "LAST_ADMIN", "系统至少需要保留一个启用的管理员");
    }
    if (request.displayName() != null) {
      String displayName = request.displayName().trim();
      if (displayName.isBlank()) {
        throw new AppException(HttpStatus.BAD_REQUEST, "INVALID_DISPLAY_NAME", "姓名不能为空");
      }
      user.setDisplayName(displayName);
    }
    if (request.password() != null) {
      user.setPasswordHash(passwordEncoder.encode(request.password()));
    }
    user.setRole(nextRole);
    user.setActive(nextActive);
    return UserResponse.from(userRepository.saveAndFlush(user));
  }

  private UserAccount requireUser(Long id) {
    return userRepository.findById(id)
        .orElseThrow(() -> new AppException(HttpStatus.NOT_FOUND, "USER_NOT_FOUND", "用户不存在"));
  }
}
