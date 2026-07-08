package com.parth.shopsphere.order.service;

import com.parth.shopsphere.cart.entity.Cart;
import com.parth.shopsphere.cart.repository.CartRepository;
import com.parth.shopsphere.common.exception.BadRequestException;
import com.parth.shopsphere.common.exception.ResourceNotFoundException;
import com.parth.shopsphere.order.dto.OrderRequest;
import com.parth.shopsphere.order.dto.OrderResponse;
import com.parth.shopsphere.order.entity.Order;
import com.parth.shopsphere.order.entity.OrderItem;
import com.parth.shopsphere.order.repository.OrderRepository;
import com.parth.shopsphere.product.entity.Product;
import com.parth.shopsphere.product.repository.ProductRepository;
import com.parth.shopsphere.user.entity.User;
import com.parth.shopsphere.user.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.List;

@Service
@RequiredArgsConstructor
public class OrderService {

    private final OrderRepository orderRepository;
    private final CartRepository cartRepository;
    private final ProductRepository productRepository;
    private final UserRepository userRepository;

    @Transactional
    public OrderResponse createOrder(String email, OrderRequest request) {
        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new ResourceNotFoundException("User not found"));

        Cart cart = cartRepository.findByUserId(user.getId())
                .orElseThrow(() -> new BadRequestException("Cart not found"));

        if (cart.getItems().isEmpty()) {
            throw new BadRequestException("Cannot create an order from an empty cart");
        }

        Order order = Order.builder()
                .user(user)
                .shippingAddress(request.getShippingAddress())
                .build();

        BigDecimal total = BigDecimal.ZERO;

        for (var cartItem : cart.getItems()) {
            Product product = cartItem.getProduct();
            
            if (!product.isActive()) {
                throw new BadRequestException("Product " + product.getName() + " is no longer available");
            }

            if (product.getStockQuantity() < cartItem.getQuantity()) {
                throw new BadRequestException("Not enough stock for product: " + product.getName());
            }

            // Deduct stock (Optimistic Locking will catch concurrency issues here)
            product.setStockQuantity(product.getStockQuantity() - cartItem.getQuantity());
            productRepository.save(product);

            OrderItem orderItem = OrderItem.builder()
                    .order(order)
                    .product(product)
                    .quantity(cartItem.getQuantity())
                    .priceAtPurchase(product.getPrice())
                    .build();
            
            order.addItem(orderItem);
            total = total.add(orderItem.getPriceAtPurchase().multiply(BigDecimal.valueOf(orderItem.getQuantity())));
        }

        order.setTotalAmount(total);

        Order savedOrder = orderRepository.save(order);

        // Clear the cart
        cart.getItems().clear();
        cartRepository.save(cart);

        return OrderResponse.fromEntity(savedOrder);
    }

    @Transactional(readOnly = true)
    public Page<OrderResponse> getUserOrders(String email, Pageable pageable) {
        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new ResourceNotFoundException("User not found"));

        return orderRepository.findByUserId(user.getId(), pageable)
                .map(OrderResponse::fromEntity);
    }

    @Transactional(readOnly = true)
    public OrderResponse getOrderById(String email, Long orderId) {
        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new ResourceNotFoundException("User not found"));

        Order order = orderRepository.findByIdAndUserId(orderId, user.getId())
                .orElseThrow(() -> new ResourceNotFoundException("Order not found or does not belong to you"));

        return OrderResponse.fromEntity(order);
    }
}
