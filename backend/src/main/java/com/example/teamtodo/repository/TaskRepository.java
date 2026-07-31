package com.example.teamtodo.repository;

import com.example.teamtodo.domain.Task;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;

import java.util.Optional;
import java.util.List;

public interface TaskRepository extends JpaRepository<Task, Long>, JpaSpecificationExecutor<Task> {
  @Override
  @EntityGraph(attributePaths = {"category", "assignee", "createdBy"})
  Page<Task> findAll(org.springframework.data.jpa.domain.Specification<Task> specification, Pageable pageable);

  @Override
  @EntityGraph(attributePaths = {"category", "assignee", "createdBy"})
  Optional<Task> findById(Long id);

  @EntityGraph(attributePaths = {"category", "assignee", "createdBy"})
  List<Task> findAllByAssigneeIsNullAndLegacyAssigneeIsNotNull();
}
