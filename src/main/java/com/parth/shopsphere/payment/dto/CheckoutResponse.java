package com.parth.shopsphere.payment.dto;

import com.parth.shopsphere.order.dto.OrderResponse;
import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class CheckoutResponse {
    private OrderResponse order;
    private PaymentResponse payment;
}
