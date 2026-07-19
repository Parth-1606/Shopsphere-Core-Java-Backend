package com.parth.shopsphere.config;

import com.parth.shopsphere.category.entity.Category;
import com.parth.shopsphere.category.repository.CategoryRepository;
import com.parth.shopsphere.product.entity.Product;
import com.parth.shopsphere.product.repository.ProductRepository;
import com.parth.shopsphere.user.enums.Role;
import com.parth.shopsphere.user.entity.User;
import com.parth.shopsphere.user.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.CommandLineRunner;
import org.springframework.context.annotation.Profile;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;
import java.util.List;

/**
 * Seeds the database with initial data only when empty.
 * Safe with persistent H2 — will not overwrite uploaded products.
 */
@Component
@Profile("local")
@RequiredArgsConstructor
@Slf4j
public class DataSeeder implements CommandLineRunner {

    private final CategoryRepository categoryRepository;
    private final ProductRepository productRepository;
    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;

    @Override
    public void run(String... args) throws Exception {
        if (userRepository.count() == 0) {
            log.info("Creating default test users...");
            userRepository.save(User.builder()
                    .firstName("Test")
                    .lastName("User")
                    .email("parth@gmail.com")
                    .passwordHash(passwordEncoder.encode("Password123"))
                    .role(Role.ROLE_USER)
                    .isActive(true)
                    .build());

            userRepository.save(User.builder()
                    .firstName("Shop")
                    .lastName("Admin")
                    .email("admin@shopsphere.com")
                    .passwordHash(passwordEncoder.encode("Admin123!"))
                    .role(Role.ROLE_ADMIN)
                    .isActive(true)
                    .build());
        }

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
                            .imageUrl("/assets/products/laptop.png")
                            .isActive(true)
                            .build(),
                    Product.builder()
                            .name("Nebula Smartwatch 5")
                            .description("Advanced health tracking, ECG, and 5-day battery life.")
                            .price(new BigDecimal("299.00"))
                            .stockQuantity(42)
                            .category(wearables)
                            .slug("nebula-smartwatch-5")
                            .imageUrl("/assets/products/smartwatch.png")
                            .isActive(true)
                            .build(),
                    Product.builder()
                            .name("Sonic Noise-Cancelling Earbuds")
                            .description("Industry-leading ANC with spatial audio and 30-hour battery.")
                            .price(new BigDecimal("199.50"))
                            .stockQuantity(120)
                            .category(audio)
                            .slug("sonic-earbuds")
                            .imageUrl("/assets/products/earbuds.png")
                            .isActive(true)
                            .build(),
                    Product.builder()
                            .name("Developer Mechanical Keyboard")
                            .description("Tactile switches, RGB backlighting, and programmable macros.")
                            .price(new BigDecimal("149.99"))
                            .stockQuantity(5)
                            .category(electronics)
                            .slug("dev-keyboard")
                            .imageUrl("/assets/products/keyboard.png")
                            .isActive(true)
                            .build()
            ));

            log.info("Data seeding complete!");
        }
    }
}
