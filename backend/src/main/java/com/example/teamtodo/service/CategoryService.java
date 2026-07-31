package com.example.teamtodo.service;

import com.example.teamtodo.api.dto.CategoryRequest;
import com.example.teamtodo.api.dto.CategoryResponse;
import com.example.teamtodo.domain.TaskCategory;
import com.example.teamtodo.exception.AppException;
import com.example.teamtodo.repository.TaskCategoryRepository;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
public class CategoryService {
  private final TaskCategoryRepository categoryRepository;

  public CategoryService(TaskCategoryRepository categoryRepository) {
    this.categoryRepository = categoryRepository;
  }

  @Transactional(readOnly = true)
  public List<CategoryResponse> list(boolean includeInactive) {
    List<TaskCategory> categories = includeInactive
        ? categoryRepository.findAllByOrderByActiveDescSortOrderAscNameAsc()
        : categoryRepository.findAllByActiveTrueOrderBySortOrderAscNameAsc();
    return categories.stream().map(CategoryResponse::from).toList();
  }

  @Transactional
  public CategoryResponse create(CategoryRequest request) {
    ensureNameAvailable(request.name(), null);
    TaskCategory category = new TaskCategory();
    apply(category, request, true);
    return CategoryResponse.from(categoryRepository.saveAndFlush(category));
  }

  @Transactional
  public CategoryResponse update(Long id, CategoryRequest request) {
    TaskCategory category = requireCategory(id);
    ensureNameAvailable(request.name(), id);
    apply(category, request, false);
    return CategoryResponse.from(categoryRepository.saveAndFlush(category));
  }

  @Transactional
  public void disable(Long id) {
    TaskCategory category = requireCategory(id);
    category.setActive(false);
    categoryRepository.save(category);
  }

  private void apply(TaskCategory category, CategoryRequest request, boolean creating) {
    category.setName(request.name().trim());
    category.setColor(request.color().toUpperCase());
    if (request.sortOrder() != null) {
      category.setSortOrder(request.sortOrder());
    }
    if (request.active() != null) {
      category.setActive(request.active());
    } else if (creating) {
      category.setActive(true);
    }
  }

  private void ensureNameAvailable(String name, Long currentId) {
    categoryRepository.findByNameIgnoreCase(name.trim()).ifPresent(existing -> {
      if (!existing.getId().equals(currentId)) {
        throw new AppException(HttpStatus.CONFLICT, "CATEGORY_NAME_EXISTS", "分类名称已存在");
      }
    });
  }

  private TaskCategory requireCategory(Long id) {
    return categoryRepository.findById(id)
        .orElseThrow(() -> new AppException(HttpStatus.NOT_FOUND, "CATEGORY_NOT_FOUND", "任务分类不存在"));
  }
}
