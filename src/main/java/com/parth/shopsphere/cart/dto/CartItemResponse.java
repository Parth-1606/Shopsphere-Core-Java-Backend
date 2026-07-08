package com.parth.shopsphere.cart.dto;

import com.parth.shopsphere.cart.entity.CartItem;
import com.parth.shopsphere.product.dto.ProductResponse;
import lombok.Builder;
import lombok.Data;

import java.math.BigDecimal;

@Data
@Builder
public class CartItemResponse {
    private Long id;
    private ProductResponse product;
    private Integer quantity;
    private BigDecimal subtotal;

    public static CartItemResponse fromEntity(CartItem cartItem) {
        return CartItemResponse.builder()
                .id(cartItem.getId())
                .product(ProductResponse.fromEntity(cartItem.getProduct()))
                .quantity(cartItem.getQuantity())
                .subtotal(cartItem.getSubtotal())
                .build();
    }
}
