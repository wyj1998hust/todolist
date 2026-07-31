package com.example.teamtodo.domain;

import jakarta.persistence.*;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import java.time.LocalDate;
import java.time.LocalDateTime;

@Entity
@Table(name = "tasks")
public class Task {
  @Id
  @GeneratedValue(strategy = GenerationType.IDENTITY)
  private Long id;

  @Column(nullable = false, length = 255)
  private String title;

  @Column(name = "start_date", nullable = false)
  private LocalDate startDate;

  @Column(nullable = false)
  private LocalDate deadline;

  @ManyToOne(fetch = FetchType.LAZY)
  @JoinColumn(name = "category_id")
  private TaskCategory category;

  @ManyToOne(fetch = FetchType.LAZY)
  @JoinColumn(name = "assignee_id")
  private UserAccount assignee;

  @ManyToOne(fetch = FetchType.LAZY)
  @JoinColumn(name = "created_by_id")
  private UserAccount createdBy;

  @Enumerated(EnumType.STRING)
  @Column(nullable = false, length = 20)
  private TaskStatus status = TaskStatus.NOT_STARTED;

  @Column(nullable = false)
  private int progress;

  @Version
  @Column(nullable = false)
  private Long version;

  @Column(name = "legacy_assignee", length = 100)
  private String legacyAssignee;

  @CreationTimestamp
  @Column(name = "created_at", updatable = false)
  private LocalDateTime createdAt;

  @UpdateTimestamp
  @Column(name = "updated_at")
  private LocalDateTime updatedAt;

  public Long getId() { return id; }
  public String getTitle() { return title; }
  public void setTitle(String title) { this.title = title; }
  public LocalDate getStartDate() { return startDate; }
  public void setStartDate(LocalDate startDate) { this.startDate = startDate; }
  public LocalDate getDeadline() { return deadline; }
  public void setDeadline(LocalDate deadline) { this.deadline = deadline; }
  public TaskCategory getCategory() { return category; }
  public void setCategory(TaskCategory category) { this.category = category; }
  public UserAccount getAssignee() { return assignee; }
  public void setAssignee(UserAccount assignee) { this.assignee = assignee; }
  public UserAccount getCreatedBy() { return createdBy; }
  public void setCreatedBy(UserAccount createdBy) { this.createdBy = createdBy; }
  public TaskStatus getStatus() { return status; }
  public void setStatus(TaskStatus status) { this.status = status; }
  public int getProgress() { return progress; }
  public void setProgress(int progress) { this.progress = progress; }
  public Long getVersion() { return version; }
  public String getLegacyAssignee() { return legacyAssignee; }
  public void setLegacyAssignee(String legacyAssignee) { this.legacyAssignee = legacyAssignee; }
  public LocalDateTime getCreatedAt() { return createdAt; }
  public LocalDateTime getUpdatedAt() { return updatedAt; }
}
