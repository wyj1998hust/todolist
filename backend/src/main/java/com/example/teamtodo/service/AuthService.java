package com.example.teamtodo.service;

import com.example.teamtodo.api.dto.LoginRequest;
import com.example.teamtodo.domain.UserAccount;
import com.example.teamtodo.exception.AppException;
import com.example.teamtodo.repository.UserAccountRepository;
import com.example.teamtodo.security.AuthenticatedUser;
import org.springframework.http.HttpStatus;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

@Service
public class AuthService {
  private final UserAccountRepository userRepository;
  private final PasswordEncoder passwordEncoder;

  public AuthService(UserAccountRepository userRepository, PasswordEncoder passwordEncoder) {
    this.userRepository = userRepository;
    this.passwordEncoder = passwordEncoder;
  }

  public AuthenticatedUser authenticate(LoginRequest request) {
    UserAccount user = userRepository.findByUsernameAndActiveTrue(request.username().trim())
        .orElseThrow(() -> new AppException(HttpStatus.UNAUTHORIZED, "INVALID_CREDENTIALS", "用户名或密码错误"));
    if (!passwordEncoder.matches(request.password(), user.getPasswordHash())) {
      throw new AppException(HttpStatus.UNAUTHORIZED, "INVALID_CREDENTIALS", "用户名或密码错误");
    }
    return AuthenticatedUser.from(user);
  }
}
