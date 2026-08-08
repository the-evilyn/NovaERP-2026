package com.novaerp.product.service;

import com.novaerp.exception.ResourceAlreadyExistsException;
import com.novaerp.exception.ResourceNotFoundException;
import com.novaerp.product.dto.CategoryDTO;
import com.novaerp.product.entity.Category;
import com.novaerp.product.repository.CategoryRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

@Service
@Slf4j
@RequiredArgsConstructor
public class CategoryServiceImpl implements CategoryService {

    private final CategoryRepository categoryRepository;

    @Override
    @Transactional(readOnly = true)
    public List<CategoryDTO> getAllCategories() {
        return categoryRepository.findAll().stream()
                .map(CategoryDTO::fromEntity)
                .collect(Collectors.toList());
    }

    @Override
    @Transactional(readOnly = true)
    public CategoryDTO getCategoryById(Long id) {
        Category category = categoryRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Category not found with id: " + id));
        return CategoryDTO.fromEntity(category);
    }

    @Override
    @Transactional
    public CategoryDTO createCategory(CategoryDTO dto) {
        log.info("Creating category: {}", dto.getName());

        if (categoryRepository.existsByCode(dto.getCode())) {
            throw new ResourceAlreadyExistsException("Category with code " + dto.getCode() + " already exists");
        }

        Category category = Category.builder()
                .code(dto.getCode().trim().toUpperCase())
                .name(dto.getName().trim())
                .description(dto.getDescription())
                .parentId(dto.getParentId())
                .active(dto.isActive())
                .build();

        Category saved = categoryRepository.save(category);
        return CategoryDTO.fromEntity(saved);
    }

    @Override
    @Transactional
    public CategoryDTO updateCategory(Long id, CategoryDTO dto) {
        log.info("Updating category with id: {}", id);

        Category category = categoryRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Category not found with id: " + id));

        if (dto.getName() != null) {
            category.setName(dto.getName().trim());
        }
        if (dto.getDescription() != null) {
            category.setDescription(dto.getDescription());
        }
        if (dto.getParentId() != null) {
            category.setParentId(dto.getParentId());
        }
        category.setActive(dto.isActive());

        Category updated = categoryRepository.save(category);
        return CategoryDTO.fromEntity(updated);
    }

    @Override
    @Transactional
    public void deleteCategory(Long id) {
        log.info("Deleting category with id: {}", id);
        if (!categoryRepository.existsById(id)) {
            throw new ResourceNotFoundException("Category not found with id: " + id);
        }
        categoryRepository.deleteById(id);
    }
}
