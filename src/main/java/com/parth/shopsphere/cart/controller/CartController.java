package com.parth.shopsphere.cart.controller;

import com.parth.shopsphere.cart.dto.CartItemRequest;
import com.parth.shopsphere.cart.dto.CartResponse;
import com.parth.shopsphere.cart.service.CartService;
import com.parth.shopsphere.common.dto.ApiResponse;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v1/cart")
@RequiredArgsConstructor
public class CartController {

    private final CartService cartService;

    @GetMapping
    public ResponseEntity<ApiResponse<CartResponse>> getCart(Authentication authentication) {
        CartResponse response = cartService.getCart(authentication.getName());
        return ResponseEntity.ok(ApiResponse.<CartResponse>builder()
                .success(true)
                .message("Cart retrieved successfully")
                .data(response)
                .build());
    }

    @PostMapping("/items")
    public ResponseEntity<ApiResponse<CartResponse>> addItemToCart(
            Authentication authentication,
            @Valid @RequestBody CartItemRequest request
    ) {
        CartResponse response = cartService.addItemToCart(authentication.getName(), request);
        return ResponseEntity.ok(ApiResponse.<CartResponse>builder()
                .success(true)
                .message("Item added to cart")
                .data(response)
                .build());
    }

    @PutMapping("/items/{itemId}")
    public ResponseEntity<ApiResponse<CartResponse>> updateItemQuantity(
            Authentication authentication,
            @PathVariable Long itemId,
            @RequestParam Integer quantity
    ) {
        CartResponse response = cartService.updateItemQuantity(authentication.getName(), itemId, quantity);
        return ResponseEntity.ok(ApiResponse.<CartResponse>builder()
                .success(true)
                .message("Cart updated successfully")
                .data(response)
                .build());
    }

    @DeleteMapping("/items/{itemId}")
    public ResponseEntity<ApiResponse<CartResponse>> removeItemFromCart(
            Authentication authentication,
            @PathVariable Long itemId
    ) {
        CartResponse response = cartService.removeItemFromCart(authentication.getName(), itemId);
        return ResponseEntity.ok(ApiResponse.<CartResponse>builder()
                .success(true)
                .message("Item removed from cart")
                .data(response)
                .build());
    }

    @DeleteMapping
    public ResponseEntity<ApiResponse<CartResponse>> clearCart(Authentication authentication) {
        CartResponse response = cartService.clearCart(authentication.getName());
        return ResponseEntity.ok(ApiResponse.<CartResponse>builder()
                .success(true)
                .message("Cart cleared successfully")
                .data(response)
                .build());
    }
}
