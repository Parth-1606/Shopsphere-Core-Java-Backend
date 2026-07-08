package com.parth.shopsphere.cart.service;

import com.parth.shopsphere.cart.dto.CartItemRequest;
import com.parth.shopsphere.cart.dto.CartResponse;
import com.parth.shopsphere.cart.entity.Cart;
import com.parth.shopsphere.cart.entity.CartItem;
import com.parth.shopsphere.cart.repository.CartRepository;
import com.parth.shopsphere.common.exception.BadRequestException;
import com.parth.shopsphere.common.exception.ResourceNotFoundException;
import com.parth.shopsphere.product.entity.Product;
import com.parth.shopsphere.product.repository.ProductRepository;
import com.parth.shopsphere.user.entity.User;
import com.parth.shopsphere.user.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Optional;

@Service
@RequiredArgsConstructor
public class CartService {

    private final CartRepository cartRepository;
    private final ProductRepository productRepository;
    private final UserRepository userRepository;

    @Transactional(readOnly = true)
    public CartResponse getCart(String email) {
        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new ResourceNotFoundException("User not found"));
        
        Cart cart = cartRepository.findByUserId(user.getId())
                .orElseGet(() -> createCart(user));
                
        return CartResponse.fromEntity(cart);
    }

    @Transactional
    public CartResponse addItemToCart(String email, CartItemRequest request) {
        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new ResourceNotFoundException("User not found"));

        Cart cart = cartRepository.findByUserId(user.getId())
                .orElseGet(() -> createCart(user));

        Product product = productRepository.findById(request.getProductId())
                .orElseThrow(() -> new ResourceNotFoundException("Product not found"));

        if (!product.isActive()) {
            throw new BadRequestException("Product is no longer available");
        }

        if (product.getStockQuantity() < request.getQuantity()) {
            throw new BadRequestException("Not enough stock available");
        }

        // Check if item already exists in cart
        Optional<CartItem> existingItem = cart.getItems().stream()
                .filter(item -> item.getProduct().getId().equals(product.getId()))
                .findFirst();

        if (existingItem.isPresent()) {
            CartItem item = existingItem.get();
            int newQuantity = item.getQuantity() + request.getQuantity();
            if (product.getStockQuantity() < newQuantity) {
                throw new BadRequestException("Not enough stock available to add more of this item");
            }
            item.setQuantity(newQuantity);
        } else {
            CartItem newItem = CartItem.builder()
                    .cart(cart)
                    .product(product)
                    .quantity(request.getQuantity())
                    .build();
            cart.addItem(newItem);
        }

        Cart savedCart = cartRepository.save(cart);
        return CartResponse.fromEntity(savedCart);
    }

    @Transactional
    public CartResponse updateItemQuantity(String email, Long itemId, Integer quantity) {
        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new ResourceNotFoundException("User not found"));

        Cart cart = cartRepository.findByUserId(user.getId())
                .orElseThrow(() -> new ResourceNotFoundException("Cart not found"));

        CartItem item = cart.getItems().stream()
                .filter(ci -> ci.getId().equals(itemId))
                .findFirst()
                .orElseThrow(() -> new ResourceNotFoundException("Item not found in cart"));

        if (quantity <= 0) {
            cart.removeItem(item);
        } else {
            if (item.getProduct().getStockQuantity() < quantity) {
                throw new BadRequestException("Not enough stock available");
            }
            item.setQuantity(quantity);
        }

        return CartResponse.fromEntity(cartRepository.save(cart));
    }

    @Transactional
    public CartResponse removeItemFromCart(String email, Long itemId) {
        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new ResourceNotFoundException("User not found"));

        Cart cart = cartRepository.findByUserId(user.getId())
                .orElseThrow(() -> new ResourceNotFoundException("Cart not found"));

        CartItem item = cart.getItems().stream()
                .filter(ci -> ci.getId().equals(itemId))
                .findFirst()
                .orElseThrow(() -> new ResourceNotFoundException("Item not found in cart"));

        cart.removeItem(item);
        return CartResponse.fromEntity(cartRepository.save(cart));
    }

    @Transactional
    public CartResponse clearCart(String email) {
        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new ResourceNotFoundException("User not found"));

        Cart cart = cartRepository.findByUserId(user.getId())
                .orElseThrow(() -> new ResourceNotFoundException("Cart not found"));

        cart.getItems().clear();
        return CartResponse.fromEntity(cartRepository.save(cart));
    }

    private Cart createCart(User user) {
        Cart cart = Cart.builder()
                .user(user)
                .build();
        return cartRepository.save(cart);
    }
}
