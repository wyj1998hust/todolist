package com.example.teamtodo.security;

import com.example.teamtodo.domain.UserAccount;
import com.example.teamtodo.domain.UserRole;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;

import java.util.List;

public record AuthenticatedUser(Long id, String username, String displayName, UserRole role, long sessionVersion) {
  public static AuthenticatedUser from(UserAccount user) {
    return new AuthenticatedUser(user.getId(), user.getUsername(), user.getDisplayName(), user.getRole(), user.getSessionVersion());
  }

  public boolean isAdmin() {
    return role == UserRole.ADMIN;
  }

  public List<GrantedAuthority> authorities() {
    return List.of(new SimpleGrantedAuthority("ROLE_" + role.name()));
  }
}
