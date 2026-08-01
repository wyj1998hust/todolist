package com.example.teamtodo.api;

import com.example.teamtodo.api.dto.LoginRequest;
import com.example.teamtodo.api.dto.ChangePasswordRequest;
import com.example.teamtodo.api.dto.UserResponse;
import com.example.teamtodo.config.AppProperties;
import com.example.teamtodo.exception.AppException;
import com.example.teamtodo.repository.UserAccountRepository;
import com.example.teamtodo.security.AuthenticatedUser;
import com.example.teamtodo.security.JwtService;
import com.example.teamtodo.service.AuthService;
import jakarta.validation.Valid;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseCookie;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

import java.time.Duration;

@RestController
@RequestMapping("/api/auth")
public class AuthController {
  private final AuthService authService;
  private final JwtService jwtService;
  private final UserAccountRepository userRepository;
  private final AppProperties properties;

  public AuthController(AuthService authService, JwtService jwtService, UserAccountRepository userRepository,
                        AppProperties properties) {
    this.authService = authService;
    this.jwtService = jwtService;
    this.userRepository = userRepository;
    this.properties = properties;
  }

  @PostMapping("/login")
  public UserResponse login(@Valid @RequestBody LoginRequest request, jakarta.servlet.http.HttpServletResponse response) {
    AuthenticatedUser principal = authService.authenticate(request);
    ResponseCookie cookie = ResponseCookie.from(properties.getAuth().getCookieName(), jwtService.issueToken(principal))
        .httpOnly(true)
        .secure(properties.getAuth().isSecureCookie())
        .sameSite("Lax")
        .path("/")
        .maxAge(Duration.ofHours(properties.getAuth().getSessionHours()))
        .build();
    response.addHeader(HttpHeaders.SET_COOKIE, cookie.toString());
    return userRepository.findById(principal.id()).map(UserResponse::from)
        .orElseThrow(() -> new AppException(HttpStatus.UNAUTHORIZED, "UNAUTHENTICATED", "当前用户不存在"));
  }

  @GetMapping("/me")
  public UserResponse me(@AuthenticationPrincipal AuthenticatedUser principal) {
    return userRepository.findById(principal.id()).map(UserResponse::from)
        .orElseThrow(() -> new AppException(HttpStatus.UNAUTHORIZED, "UNAUTHENTICATED", "当前用户不存在"));
  }

  @PostMapping("/logout")
  public void logout(jakarta.servlet.http.HttpServletResponse response) {
    expireAuthCookie(response);
  }

  @PostMapping("/change-password")
  @ResponseStatus(HttpStatus.NO_CONTENT)
  public void changePassword(@Valid @RequestBody ChangePasswordRequest request,
                             @AuthenticationPrincipal AuthenticatedUser principal,
                             jakarta.servlet.http.HttpServletResponse response) {
    authService.changePassword(principal, request);
    expireAuthCookie(response);
  }

  private void expireAuthCookie(jakarta.servlet.http.HttpServletResponse response) {
    ResponseCookie cookie = ResponseCookie.from(properties.getAuth().getCookieName(), "")
        .httpOnly(true)
        .secure(properties.getAuth().isSecureCookie())
        .sameSite("Lax")
        .path("/")
        .maxAge(Duration.ZERO)
        .build();
    response.addHeader(HttpHeaders.SET_COOKIE, cookie.toString());
  }
}
