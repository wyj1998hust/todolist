package com.example.teamtodo.repository;

import com.example.teamtodo.domain.TaskCategory;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface TaskCategoryRepository extends JpaRepository<TaskCategory, Long> {
  List<TaskCategory> findAllByActiveTrueOrderBySortOrderAscNameAsc();
  List<TaskCategory> findAllByOrderByActiveDescSortOrderAscNameAsc();
  boolean existsByNameIgnoreCase(String name);
  Optional<TaskCategory> findByNameIgnoreCase(String name);
}
