package com.example.teamtodo.service;

import com.example.teamtodo.api.dto.UserUpdateRequest;
import com.example.teamtodo.domain.UserAccount;
import com.example.teamtodo.domain.UserRole;
import com.example.teamtodo.repository.UserAccountRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.when;
import static org.mockito.Mockito.verify;

@ExtendWith(MockitoExtension.class)
class UserServiceTest {
  @Mock
  private UserAccountRepository userRepository;
  @Mock
  private PasswordEncoder passwordEncoder;

  @Test
  void adminPasswordResetInvalidatesTargetSessions() {
    UserAccount user = new UserAccount();
    user.setRole(UserRole.MEMBER);
    user.setPasswordHash("old-hash");
    when(userRepository.findById(2L)).thenReturn(Optional.of(user));
    when(passwordEncoder.encode("new-password")).thenReturn("new-hash");
    when(userRepository.saveAndFlush(user)).thenReturn(user);

    UserService service = new UserService(userRepository, passwordEncoder);
    service.update(2L, new UserUpdateRequest(null, "new-password", null, null));

    assertEquals("new-hash", user.getPasswordHash());
    assertEquals(1L, user.getSessionVersion());
    verify(userRepository).saveAndFlush(user);
  }
}
