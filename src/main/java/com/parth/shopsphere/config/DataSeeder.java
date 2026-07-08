package com.parth.shopsphere.config;

import com.parth.shopsphere.category.entity.Category;
import com.parth.shopsphere.category.repository.CategoryRepository;
import com.parth.shopsphere.product.entity.Product;
import com.parth.shopsphere.product.repository.ProductRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.CommandLineRunner;
import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;
import java.util.List;

/**
 * Seeds the database with initial data when running locally with H2.
 * This ensures the frontend has products to display immediately.
 */
@Component
@Profile("local")
@RequiredArgsConstructor
@Slf4j
public class DataSeeder implements CommandLineRunner {

    private final CategoryRepository categoryRepository;
    private final ProductRepository productRepository;

    @Override
    public void run(String... args) throws Exception {
        if (categoryRepository.count() == 0) {
            log.info("Seeding initial data...");

            Category electronics = categoryRepository.save(Category.builder()
                    .name("Electronics")
                    .description("Gadgets and devices")
                    .slug("electronics")
                    .build());

            Category wearables = categoryRepository.save(Category.builder()
                    .name("Wearables")
                    .description("Smartwatches and fitness trackers")
                    .slug("wearables")
                    .build());

            Category audio = categoryRepository.save(Category.builder()
                    .name("Audio")
                    .description("Headphones and speakers")
                    .slug("audio")
                    .build());

            productRepository.saveAll(List.of(
                    Product.builder()
                            .name("Quantum Pro Laptop")
                            .description("Ultra-fast M3 processor with 32GB RAM and 1TB SSD. Perfect for developers.")
                            .price(new BigDecimal("1899.99"))
                            .stockQuantity(15)
                            .category(electronics)
                            .slug("quantum-pro-laptop")
                            .isActive(true)
                            .build(),
                    Product.builder()
                            .name("Nebula Smartwatch 5")
                            .description("Advanced health tracking, ECG, and 5-day battery life.")
                            .price(new BigDecimal("299.00"))
                            .stockQuantity(42)
                            .category(wearables)
                            .slug("nebula-smartwatch-5")
                            .isActive(true)
                            .build(),
                    Product.builder()
                            .name("Sonic Noise-Cancelling Earbuds")
                            .description("Industry-leading ANC with spatial audio and 30-hour battery.")
                            .price(new BigDecimal("199.50"))
                            .stockQuantity(120)
                            .category(audio)
                            .slug("sonic-earbuds")
                            .isActive(true)
                            .build(),
                    Product.builder()
                            .name("Developer Mechanical Keyboard")
                            .description("Tactile switches, RGB backlighting, and programmable macros.")
                            .price(new BigDecimal("149.99"))
                            .stockQuantity(5)
                            .category(electronics)
                            .slug("dev-keyboard")
                            .isActive(true)
                            .build()
            ));

            log.info("Data seeding complete!");
        }
    }
}
