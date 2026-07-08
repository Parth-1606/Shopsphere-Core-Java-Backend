package com.parth.shopsphere.product.dto;

import com.parth.shopsphere.category.dto.CategoryResponse;
import com.parth.shopsphere.product.entity.Product;
import lombok.Builder;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Builder
public record ProductResponse(
        Long id,
        String name,
        String slug,
        String description,
        BigDecimal price,
        Integer stockQuantity,
        String imageUrl,
        boolean isActive,
        CategoryResponse category,
        LocalDateTime createdAt,
        LocalDateTime updatedAt
) {
    public static ProductResponse fromEntity(Product product) {
        return ProductResponse.builder()
                .id(product.getId())
                .name(product.getName())
                .slug(product.getSlug())
                .description(product.getDescription())
                .price(product.getPrice())
                .stockQuantity(product.getStockQuantity())
                .imageUrl(product.getImageUrl())
                .isActive(product.isActive())
                .category(CategoryResponse.fromEntity(product.getCategory()))
                .createdAt(product.getCreatedAt())
                .updatedAt(product.getUpdatedAt())
                .build();
    }
}
