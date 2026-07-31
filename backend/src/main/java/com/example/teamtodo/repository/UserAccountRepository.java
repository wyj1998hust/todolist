package com.example.teamtodo.repository;

import com.example.teamtodo.domain.UserAccount;
import com.example.teamtodo.domain.UserRole;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface UserAccountRepository extends JpaRepository<UserAccount, Long> {
  Optional<UserAccount> findByUsernameAndActiveTrue(String username);
  boolean existsByUsernameIgnoreCase(String username);
  long countByRoleAndActiveTrue(UserRole role);
  List<UserAccount> findAllByActiveTrueOrderByDisplayNameAsc();
  List<UserAccount> findAllByOrderByDisplayNameAsc();
  Optional<UserAccount> findByDisplayNameIgnoreCase(String displayName);
  Optional<UserAccount> findByUsernameIgnoreCase(String username);
}
