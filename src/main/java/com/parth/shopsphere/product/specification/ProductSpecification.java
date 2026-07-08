package com.parth.shopsphere.product.specification;

import com.parth.shopsphere.product.entity.Product;
import jakarta.persistence.criteria.Predicate;
import org.springframework.data.jpa.domain.Specification;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;

public class ProductSpecification {

    public static Specification<Product> filterProducts(
            String query, 
            String categorySlug, 
            BigDecimal minPrice, 
            BigDecimal maxPrice
    ) {
        return (root, cq, cb) -> {
            List<Predicate> predicates = new ArrayList<>();

            // Always only show active products for public search
            predicates.add(cb.isTrue(root.get("isActive")));

            if (query != null && !query.trim().isEmpty()) {
                String likePattern = "%" + query.toLowerCase() + "%";
                Predicate nameMatch = cb.like(cb.lower(root.get("name")), likePattern);
                Predicate descMatch = cb.like(cb.lower(root.get("description")), likePattern);
                predicates.add(cb.or(nameMatch, descMatch));
            }

            if (categorySlug != null && !categorySlug.trim().isEmpty()) {
                predicates.add(cb.equal(root.get("category").get("slug"), categorySlug));
            }

            if (minPrice != null) {
                predicates.add(cb.greaterThanOrEqualTo(root.get("price"), minPrice));
            }

            if (maxPrice != null) {
                predicates.add(cb.lessThanOrEqualTo(root.get("price"), maxPrice));
            }

            return cb.and(predicates.toArray(new Predicate[0]));
        };
    }
}
