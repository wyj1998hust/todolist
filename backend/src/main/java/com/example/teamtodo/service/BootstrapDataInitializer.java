package com.example.teamtodo.service;

import com.example.teamtodo.config.AppProperties;
import com.example.teamtodo.domain.Task;
import com.example.teamtodo.domain.TaskCategory;
import com.example.teamtodo.domain.UserAccount;
import com.example.teamtodo.domain.UserRole;
import com.example.teamtodo.repository.TaskCategoryRepository;
import com.example.teamtodo.repository.TaskRepository;
import com.example.teamtodo.repository.UserAccountRepository;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.util.Arrays;

@Component
@ConditionalOnProperty(name = "app.migrate-only", havingValue = "false", matchIfMissing = true)
public class BootstrapDataInitializer implements ApplicationRunner {
  private final AppProperties properties;
  private final UserAccountRepository userRepository;
  private final TaskCategoryRepository categoryRepository;
  private final TaskRepository taskRepository;
  private final PasswordEncoder passwordEncoder;

  public BootstrapDataInitializer(AppProperties properties, UserAccountRepository userRepository,
                                  TaskCategoryRepository categoryRepository, TaskRepository taskRepository,
                                  PasswordEncoder passwordEncoder) {
    this.properties = properties;
    this.userRepository = userRepository;
    this.categoryRepository = categoryRepository;
    this.taskRepository = taskRepository;
    this.passwordEncoder = passwordEncoder;
  }

  @Override
  @Transactional
  public void run(ApplicationArguments args) {
    if (!properties.getBootstrap().isEnabled()) {
      return;
    }
    createUserIfAbsent(properties.getBootstrap().getAdminUsername(), properties.getBootstrap().getAdminDisplayName(),
        properties.getBootstrap().getAdminPassword(), UserRole.ADMIN);
    String memberPassword = properties.getBootstrap().getMemberPassword();
    String memberUsernames = properties.getBootstrap().getMemberUsernames();
    if (memberPassword != null && memberUsernames != null) {
      String[] names = Arrays.stream(memberUsernames.split(",")).map(String::trim).filter(value -> !value.isBlank()).toArray(String[]::new);
      for (int index = 0; index < names.length; index++) {
        createUserIfAbsent(names[index], "成员 " + (index + 1), memberPassword, UserRole.MEMBER);
      }
    }
    TaskCategory uncategorized = categoryRepository.findByNameIgnoreCase("未分类").orElseGet(() -> {
      TaskCategory category = new TaskCategory();
      category.setName("未分类");
      category.setColor("#64748B");
      category.setSortOrder(0);
      category.setActive(true);
      return categoryRepository.save(category);
    });
    taskRepository.findAllByAssigneeIsNullAndLegacyAssigneeIsNotNull().forEach(task -> mapLegacyAssignment(task, uncategorized));
  }

  private void createUserIfAbsent(String username, String displayName, String password, UserRole role) {
    if (username == null || username.isBlank() || password == null || password.length() < 8
        || userRepository.existsByUsernameIgnoreCase(username.trim())) {
      return;
    }
    UserAccount user = new UserAccount();
    user.setUsername(username.trim());
    user.setDisplayName(displayName == null || displayName.isBlank() ? username.trim() : displayName.trim());
    user.setPasswordHash(passwordEncoder.encode(password));
    user.setRole(role);
    user.setActive(true);
    userRepository.save(user);
  }

  private void mapLegacyAssignment(Task task, TaskCategory uncategorized) {
    String legacyName = task.getLegacyAssignee().trim();
    userRepository.findByDisplayNameIgnoreCase(legacyName)
        .or(() -> userRepository.findByUsernameIgnoreCase(legacyName))
        .ifPresent(task::setAssignee);
    if (task.getCategory() == null) {
      task.setCategory(uncategorized);
    }
  }
}
