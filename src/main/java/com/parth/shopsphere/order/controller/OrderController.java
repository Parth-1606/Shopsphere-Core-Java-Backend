package com.parth.shopsphere.order.controller;

import com.parth.shopsphere.common.dto.ApiResponse;
import com.parth.shopsphere.order.dto.OrderRequest;
import com.parth.shopsphere.order.dto.OrderResponse;
import com.parth.shopsphere.order.service.OrderService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v1/orders")
@RequiredArgsConstructor
public class OrderController {

    private final OrderService orderService;

    @PostMapping
    public ResponseEntity<ApiResponse<OrderResponse>> createOrder(
            Authentication authentication,
            @Valid @RequestBody OrderRequest request
    ) {
        OrderResponse response = orderService.createOrder(authentication.getName(), request);
        return ResponseEntity.status(HttpStatus.CREATED).body(ApiResponse.<OrderResponse>builder()
                .success(true)
                .message("Order created successfully")
                .data(response)
                .build());
    }

    @GetMapping
    public ResponseEntity<ApiResponse<Page<OrderResponse>>> getUserOrders(
            Authentication authentication,
            @PageableDefault(size = 10, sort = "createdAt,desc") Pageable pageable
    ) {
        Page<OrderResponse> response = orderService.getUserOrders(authentication.getName(), pageable);
        return ResponseEntity.ok(ApiResponse.<Page<OrderResponse>>builder()
                .success(true)
                .message("Orders retrieved successfully")
                .data(response)
                .build());
    }

    @GetMapping("/{id}")
    public ResponseEntity<ApiResponse<OrderResponse>> getOrderById(
            Authentication authentication,
            @PathVariable Long id
    ) {
        OrderResponse response = orderService.getOrderById(authentication.getName(), id);
        return ResponseEntity.ok(ApiResponse.<OrderResponse>builder()
                .success(true)
                .message("Order retrieved successfully")
                .data(response)
                .build());
    }
}
