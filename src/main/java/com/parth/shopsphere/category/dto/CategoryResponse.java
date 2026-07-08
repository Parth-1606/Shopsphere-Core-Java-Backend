package com.parth.shopsphere.category.dto;

import com.parth.shopsphere.category.entity.Category;
import lombok.Builder;

import java.time.LocalDateTime;

@Builder
public record CategoryResponse(
        Long id,
        String name,
        String slug,
        String description,
        LocalDateTime createdAt,
        LocalDateTime updatedAt
) {
    public static CategoryResponse fromEntity(Category category) {
        return CategoryResponse.builder()
                .id(category.getId())
                .name(category.getName())
                .slug(category.getSlug())
                .description(category.getDescription())
                .createdAt(category.getCreatedAt())
                .updatedAt(category.getUpdatedAt())
                .build();
    }
}
