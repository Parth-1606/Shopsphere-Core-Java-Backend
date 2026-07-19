package com.parth.shopsphere.payment.controller;

import com.parth.shopsphere.common.dto.ApiResponse;
import com.parth.shopsphere.payment.dto.CheckoutRequest;
import com.parth.shopsphere.payment.dto.CheckoutResponse;
import com.parth.shopsphere.payment.service.PaymentService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1/payments")
@RequiredArgsConstructor
public class PaymentController {

    private final PaymentService paymentService;

    @PostMapping("/checkout")
    public ResponseEntity<ApiResponse<CheckoutResponse>> checkout(
            Authentication authentication,
            @Valid @RequestBody CheckoutRequest request
    ) {
        CheckoutResponse response = paymentService.checkout(authentication.getName(), request);
        return ResponseEntity.status(HttpStatus.CREATED).body(ApiResponse.<CheckoutResponse>builder()
                .success(true)
                .message("Checkout completed successfully")
                .data(response)
                .build());
    }
}
