package com.example.JWTImplemenation.Controller;

import com.example.JWTImplemenation.DTO.CartDTO;
import com.example.JWTImplemenation.DTO.CartItemDTO;
import com.example.JWTImplemenation.Entities.CartItem;
import com.example.JWTImplemenation.Service.IService.ICartService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v1/cart")
public class CartController {

    @Autowired
    private ICartService cartService;

    @GetMapping("/{userId}")
    public ResponseEntity<CartDTO> getCartByUserId(@PathVariable Integer userId) {
        return cartService.findCartByUserId(userId);
    }

    @PostMapping("/{userId}")
    public ResponseEntity<CartItemDTO> addToCart(@PathVariable Integer userId, @RequestBody CartItemDTO cartItem) {
        return cartService.addToCart(userId, cartItem);
    }

    @DeleteMapping("/{userId}/{cartItemId}")
    public ResponseEntity<Void> removeFromCart(@PathVariable Integer userId, @PathVariable Integer cartItemId) {
        return cartService.removeFromCart(userId, cartItemId);
    }
    @PostMapping("/clear/{userId}")
    public ResponseEntity<Void> clearCart(@PathVariable Integer userId) {
        cartService.clearCart(userId);
        return ResponseEntity.noContent().build();
    }

    @PutMapping("/{userId}/item/{cartItemId}")
    public ResponseEntity<CartDTO> updateCartItemQuantity(
            @PathVariable Integer userId,
            @PathVariable Integer cartItemId,
            @RequestParam Integer quantity) {
        return cartService.updateCartItemQuantity(userId, cartItemId, quantity);
    }
    @PutMapping("/{userId}/apply-voucher")
    public ResponseEntity<CartDTO> applyVoucher(@PathVariable Integer userId, @RequestParam String voucherCode) {
        try {
            CartDTO updatedCart = cartService.applyVoucher(userId, voucherCode);
            return ResponseEntity.ok(updatedCart);
        } catch (Exception e) {
            return ResponseEntity.badRequest().build();
        }
    }
}

