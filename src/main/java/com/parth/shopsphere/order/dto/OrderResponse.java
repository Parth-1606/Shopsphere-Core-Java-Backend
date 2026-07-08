package com.parth.shopsphere.order.dto;

import com.parth.shopsphere.order.entity.Order;
import lombok.Builder;
import lombok.Data;

import java.math.BigDecimal;
import java.util.List;
import java.util.stream.Collectors;

@Data
@Builder
public class OrderResponse {
    private Long id;
    private Long userId;
    private String shippingAddress;
    private String status;
    private BigDecimal totalAmount;
    private List<OrderItemResponse> items;
    private String createdAt;

    public static OrderResponse fromEntity(Order order) {
        return OrderResponse.builder()
                .id(order.getId())
                .userId(order.getUser().getId())
                .shippingAddress(order.getShippingAddress())
                .status(order.getStatus().name())
                .totalAmount(order.getTotalAmount())
                .items(order.getItems().stream()
                        .map(OrderItemResponse::fromEntity)
                        .collect(Collectors.toList()))
                .createdAt(order.getCreatedAt().toString())
                .build();
    }
}
