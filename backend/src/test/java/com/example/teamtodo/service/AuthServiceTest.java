package com.example.teamtodo.service;

import com.example.teamtodo.api.dto.ChangePasswordRequest;
import com.example.teamtodo.domain.UserAccount;
import com.example.teamtodo.domain.UserRole;
import com.example.teamtodo.exception.AppException;
import com.example.teamtodo.repository.UserAccountRepository;
import com.example.teamtodo.security.AuthenticatedUser;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class AuthServiceTest {
  @Mock
  private UserAccountRepository userRepository;
  @Mock
  private PasswordEncoder passwordEncoder;

  private AuthService service;

  @BeforeEach
  void setUp() {
    service = new AuthService(userRepository, passwordEncoder);
  }

  @Test
  void changesPasswordAndInvalidatesSessionVersion() {
    UserAccount user = new UserAccount();
    user.setPasswordHash("old-hash");
    when(userRepository.findById(1L)).thenReturn(Optional.of(user));
    when(passwordEncoder.matches("old-password", "old-hash")).thenReturn(true);
    when(passwordEncoder.encode("new-password")).thenReturn("new-hash");

    service.changePassword(new AuthenticatedUser(1L, "member1", "成员 1", UserRole.MEMBER, 0),
        new ChangePasswordRequest("old-password", "new-password"));

    assertEquals("new-hash", user.getPasswordHash());
    assertEquals(1L, user.getSessionVersion());
    verify(userRepository).saveAndFlush(user);
  }

  @Test
  void rejectsWrongCurrentPasswordWithoutChangingUser() {
    UserAccount user = new UserAccount();
    user.setPasswordHash("old-hash");
    when(userRepository.findById(1L)).thenReturn(Optional.of(user));
    when(passwordEncoder.matches("wrong-password", "old-hash")).thenReturn(false);

    AppException exception = assertThrows(AppException.class, () -> service.changePassword(
        new AuthenticatedUser(1L, "member1", "成员 1", UserRole.MEMBER, 0),
        new ChangePasswordRequest("wrong-password", "new-password")));

    assertEquals("INVALID_CURRENT_PASSWORD", exception.getCode());
    assertEquals(0L, user.getSessionVersion());
    verify(passwordEncoder, never()).encode(anyString());
    verify(userRepository, never()).saveAndFlush(any());
  }
}
