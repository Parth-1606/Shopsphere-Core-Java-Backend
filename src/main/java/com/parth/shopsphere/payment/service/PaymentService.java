package com.parth.shopsphere.payment.service;

import com.parth.shopsphere.common.exception.BadRequestException;
import com.parth.shopsphere.order.dto.OrderRequest;
import com.parth.shopsphere.order.dto.OrderResponse;
import com.parth.shopsphere.order.entity.Order;
import com.parth.shopsphere.order.entity.OrderStatus;
import com.parth.shopsphere.order.repository.OrderRepository;
import com.parth.shopsphere.order.service.OrderService;
import com.parth.shopsphere.payment.dto.CheckoutRequest;
import com.parth.shopsphere.payment.dto.CheckoutResponse;
import com.parth.shopsphere.payment.dto.PaymentResponse;
import com.parth.shopsphere.payment.entity.Payment;
import com.parth.shopsphere.payment.entity.PaymentMethod;
import com.parth.shopsphere.payment.entity.PaymentStatus;
import com.parth.shopsphere.payment.repository.PaymentRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.UUID;

@Service
@RequiredArgsConstructor
public class PaymentService {

    private final OrderService orderService;
    private final OrderRepository orderRepository;
    private final PaymentRepository paymentRepository;

    @Transactional
    public CheckoutResponse checkout(String email, CheckoutRequest request) {
        String cardLast4 = null;

        if (request.getPaymentMethod() == PaymentMethod.CARD) {
            cardLast4 = validateAndAuthorizeCard(request);
        }

        OrderRequest orderRequest = new OrderRequest();
        orderRequest.setShippingAddress(request.getShippingAddress());
        OrderResponse orderResponse = orderService.createOrder(email, orderRequest);

        Order order = orderRepository.findById(orderResponse.getId())
                .orElseThrow(() -> new BadRequestException("Order could not be created"));

        Payment payment = Payment.builder()
                .order(order)
                .method(request.getPaymentMethod())
                .amount(order.getTotalAmount())
                .status(PaymentStatus.SUCCESS)
                .cardLast4(cardLast4)
                .transactionId(buildTransactionId(request.getPaymentMethod()))
                .build();

        order.setStatus(OrderStatus.PROCESSING);
        Payment saved = paymentRepository.save(payment);
        orderRepository.save(order);

        return CheckoutResponse.builder()
                .order(OrderResponse.fromEntity(order))
                .payment(PaymentResponse.fromEntity(saved))
                .build();
    }

    private String validateAndAuthorizeCard(CheckoutRequest request) {
        if (request.getCardNumber() == null || request.getCardExpiry() == null || request.getCardCvv() == null) {
            throw new BadRequestException("Card number, expiry, and CVV are required for card payments");
        }

        String digits = request.getCardNumber().replaceAll("\\D", "");
        if (digits.length() < 13 || digits.length() > 19) {
            throw new BadRequestException("Enter a valid card number");
        }

        if (!request.getCardExpiry().matches("^(0[1-9]|1[0-2])/\\d{2}$")) {
            throw new BadRequestException("Card expiry must be in MM/YY format");
        }

        String cvv = request.getCardCvv().replaceAll("\\D", "");
        if (cvv.length() < 3 || cvv.length() > 4) {
            throw new BadRequestException("Enter a valid CVV");
        }

        // Demo decline rule — cards ending in 0000 always fail
        if (digits.endsWith("0000")) {
            throw new BadRequestException("Payment failed: card declined. Use another card or Cash on Delivery.");
        }

        return digits.substring(digits.length() - 4);
    }

    private String buildTransactionId(PaymentMethod method) {
        String prefix = method == PaymentMethod.COD ? "COD" : "TXN";
        return prefix + "-" + UUID.randomUUID().toString().substring(0, 8).toUpperCase();
    }
}
