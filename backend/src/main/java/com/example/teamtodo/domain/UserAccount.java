package com.example.teamtodo.domain;

import jakarta.persistence.*;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import java.time.LocalDateTime;

@Entity
@Table(name = "users")
public class UserAccount {
  @Id
  @GeneratedValue(strategy = GenerationType.IDENTITY)
  private Long id;

  @Column(nullable = false, unique = true, length = 64)
  private String username;

  @Column(name = "password_hash", nullable = false, length = 100)
  private String passwordHash;

  @Column(name = "display_name", nullable = false, length = 100)
  private String displayName;

  @Enumerated(EnumType.STRING)
  @Column(nullable = false, length = 20)
  private UserRole role;

  @Column(name = "is_active", nullable = false)
  private boolean active = true;

  @Column(name = "session_version", nullable = false)
  private long sessionVersion = 0;

  @CreationTimestamp
  @Column(name = "created_at", updatable = false)
  private LocalDateTime createdAt;

  @UpdateTimestamp
  @Column(name = "updated_at")
  private LocalDateTime updatedAt;

  public Long getId() { return id; }
  public String getUsername() { return username; }
  public void setUsername(String username) { this.username = username; }
  public String getPasswordHash() { return passwordHash; }
  public void setPasswordHash(String passwordHash) { this.passwordHash = passwordHash; }
  public String getDisplayName() { return displayName; }
  public void setDisplayName(String displayName) { this.displayName = displayName; }
  public UserRole getRole() { return role; }
  public void setRole(UserRole role) { this.role = role; }
  public boolean isActive() { return active; }
  public void setActive(boolean active) { this.active = active; }
  public long getSessionVersion() { return sessionVersion; }
  public void incrementSessionVersion() { sessionVersion++; }
  public LocalDateTime getCreatedAt() { return createdAt; }
  public LocalDateTime getUpdatedAt() { return updatedAt; }
}
