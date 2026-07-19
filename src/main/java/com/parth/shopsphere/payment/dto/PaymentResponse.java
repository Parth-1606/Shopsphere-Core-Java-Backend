package com.parth.shopsphere.payment.dto;

import com.parth.shopsphere.payment.entity.Payment;
import lombok.Builder;
import lombok.Data;

import java.math.BigDecimal;

@Data
@Builder
public class PaymentResponse {
    private Long id;
    private Long orderId;
    private String method;
    private String status;
    private BigDecimal amount;
    private String transactionId;
    private String cardLast4;
    private String failureReason;

    public static PaymentResponse fromEntity(Payment payment) {
        return PaymentResponse.builder()
                .id(payment.getId())
                .orderId(payment.getOrder().getId())
                .method(payment.getMethod().name())
                .status(payment.getStatus().name())
                .amount(payment.getAmount())
                .transactionId(payment.getTransactionId())
                .cardLast4(payment.getCardLast4())
                .failureReason(payment.getFailureReason())
                .build();
    }
}
