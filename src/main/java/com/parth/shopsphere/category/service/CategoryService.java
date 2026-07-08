package com.parth.shopsphere.category.service;

import com.parth.shopsphere.category.dto.CategoryRequest;
import com.parth.shopsphere.category.dto.CategoryResponse;
import com.parth.shopsphere.category.entity.Category;
import com.parth.shopsphere.category.repository.CategoryRepository;
import com.parth.shopsphere.common.exception.BadRequestException;
import com.parth.shopsphere.common.exception.ResourceNotFoundException;
import com.parth.shopsphere.common.util.SlugUtil;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class CategoryService {

    private final CategoryRepository categoryRepository;

    @Transactional(readOnly = true)
    public Page<CategoryResponse> getAllCategories(Pageable pageable) {
        return categoryRepository.findAll(pageable)
                .map(CategoryResponse::fromEntity);
    }

    @Transactional(readOnly = true)
    public CategoryResponse getCategoryBySlug(String slug) {
        Category category = categoryRepository.findBySlug(slug)
                .orElseThrow(() -> new ResourceNotFoundException("Category not found with slug: " + slug));
        return CategoryResponse.fromEntity(category);
    }

    @Transactional
    public CategoryResponse createCategory(CategoryRequest request) {
        if (categoryRepository.existsByName(request.name())) {
            throw new BadRequestException("Category with this name already exists");
        }

        Category category = Category.builder()
                .name(request.name())
                .slug(SlugUtil.toSlug(request.name()))
                .description(request.description())
                .build();

        return CategoryResponse.fromEntity(categoryRepository.save(category));
    }

    @Transactional
    public CategoryResponse updateCategory(Long id, CategoryRequest request) {
        Category category = categoryRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Category not found with id: " + id));

        if (!category.getName().equals(request.name()) && categoryRepository.existsByName(request.name())) {
            throw new BadRequestException("Category with this name already exists");
        }

        category.setName(request.name());
        category.setSlug(SlugUtil.toSlug(request.name()));
        category.setDescription(request.description());

        return CategoryResponse.fromEntity(categoryRepository.save(category));
    }

    @Transactional
    public void deleteCategory(Long id) {
        if (!categoryRepository.existsById(id)) {
            throw new ResourceNotFoundException("Category not found with id: " + id);
        }
        categoryRepository.deleteById(id);
    }
}
