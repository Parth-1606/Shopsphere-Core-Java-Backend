package com.parth.shopsphere.category.dto;

import jakarta.validation.constraints.NotBlank;
import lombok.Builder;

@Builder
public record CategoryRequest(
        @NotBlank(message = "Category name is required")
        String name,
        
        String description
) {}
