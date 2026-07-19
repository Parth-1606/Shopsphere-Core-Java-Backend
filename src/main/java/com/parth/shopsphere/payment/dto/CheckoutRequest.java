package com.parth.shopsphere.payment.dto;

import com.parth.shopsphere.payment.entity.PaymentMethod;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

@Data
public class CheckoutRequest {

    @NotBlank(message = "Shipping address is required")
    private String shippingAddress;

    @NotNull(message = "Payment method is required")
    private PaymentMethod paymentMethod;

    /** Digits only, 13–19 length. Required when paymentMethod is CARD. */
    private String cardNumber;

    /** MM/YY format. Required when paymentMethod is CARD. */
    private String cardExpiry;

    /** 3–4 digits. Required when paymentMethod is CARD. */
    private String cardCvv;
}
