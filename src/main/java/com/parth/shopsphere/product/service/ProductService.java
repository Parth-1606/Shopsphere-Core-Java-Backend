package com.parth.shopsphere.product.service;

import com.parth.shopsphere.category.entity.Category;
import com.parth.shopsphere.category.repository.CategoryRepository;
import com.parth.shopsphere.common.exception.BadRequestException;
import com.parth.shopsphere.common.exception.ResourceNotFoundException;
import com.parth.shopsphere.common.service.FileStorageService;
import com.parth.shopsphere.common.util.SlugUtil;
import com.parth.shopsphere.product.dto.ProductRequest;
import com.parth.shopsphere.product.dto.ProductResponse;
import com.parth.shopsphere.product.entity.Product;
import com.parth.shopsphere.product.repository.ProductRepository;
import com.parth.shopsphere.product.specification.ProductSpecification;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.math.BigDecimal;

@Service
@RequiredArgsConstructor
public class ProductService {

    private final ProductRepository productRepository;
    private final CategoryRepository categoryRepository;
    private final FileStorageService fileStorageService;

    @Transactional(readOnly = true)
    public Page<ProductResponse> searchProducts(
            String query,
            String categorySlug,
            BigDecimal minPrice,
            BigDecimal maxPrice,
            Pageable pageable
    ) {
        Specification<Product> spec = ProductSpecification.filterProducts(query, categorySlug, minPrice, maxPrice);
        return productRepository.findAll(spec, pageable).map(ProductResponse::fromEntity);
    }

    @Transactional(readOnly = true)
    public ProductResponse getProductBySlug(String slug) {
        Product product = productRepository.findBySlugAndIsActiveTrue(slug)
                .orElseThrow(() -> new ResourceNotFoundException("Product not found with slug: " + slug));
        return ProductResponse.fromEntity(product);
    }

    @Transactional
    public ProductResponse createProduct(ProductRequest request) {
        if (productRepository.existsByName(request.name())) {
            throw new BadRequestException("Product with this name already exists");
        }

        Category category = categoryRepository.findById(request.categoryId())
                .orElseThrow(() -> new ResourceNotFoundException("Category not found"));

        Product product = Product.builder()
                .name(request.name())
                .slug(SlugUtil.toSlug(request.name()))
                .description(request.description())
                .price(request.price())
                .stockQuantity(request.stockQuantity())
                .imageUrl(request.imageUrl())
                .category(category)
                .isActive(true)
                .build();

        return ProductResponse.fromEntity(productRepository.save(product));
    }

    @Transactional
    public ProductResponse updateProduct(Long id, ProductRequest request) {
        Product product = productRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Product not found"));

        if (!product.getName().equals(request.name()) && productRepository.existsByName(request.name())) {
            throw new BadRequestException("Product with this name already exists");
        }

        Category category = categoryRepository.findById(request.categoryId())
                .orElseThrow(() -> new ResourceNotFoundException("Category not found"));

        product.setName(request.name());
        product.setSlug(SlugUtil.toSlug(request.name()));
        product.setDescription(request.description());
        product.setPrice(request.price());
        product.setStockQuantity(request.stockQuantity());
        product.setImageUrl(request.imageUrl());
        product.setCategory(category);

        return ProductResponse.fromEntity(productRepository.save(product));
    }

    @Transactional
    public void deleteProduct(Long id) {
        Product product = productRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Product not found"));
        // Soft delete
        product.setActive(false);
        productRepository.save(product);
    }

    @Transactional
    public ProductResponse uploadImage(Long id, MultipartFile file) {
        Product product = productRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Product not found"));

        if (product.getImageUrl() != null && !product.getImageUrl().isEmpty()) {
            fileStorageService.deleteFile(product.getImageUrl());
        }

        String imageUrl = fileStorageService.storeFile(file, "product_" + id);
        product.setImageUrl(imageUrl);
        return ProductResponse.fromEntity(productRepository.save(product));
    }
}
