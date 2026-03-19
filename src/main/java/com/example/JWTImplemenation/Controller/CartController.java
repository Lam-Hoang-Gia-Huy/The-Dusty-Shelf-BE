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
    public ResponseEntity<CartDTO> getCartByUserId(@PathVariable("userId") Integer userId) {
        return cartService.findCartByUserId(userId);
    }

    @PostMapping("/{userId}")
    public ResponseEntity<CartItemDTO> addToCart(@PathVariable("userId") Integer userId,
            @RequestBody CartItemDTO cartItem) {
        return cartService.addToCart(userId, cartItem);
    }

    @DeleteMapping("/{userId}/{cartItemId}")
    public ResponseEntity<Void> removeFromCart(@PathVariable("userId") Integer userId,
            @PathVariable("cartItemId") Integer cartItemId) {
        return cartService.removeFromCart(userId, cartItemId);
    }

    @PostMapping("/clear/{userId}")
    public ResponseEntity<Void> clearCart(@PathVariable("userId") Integer userId) {
        cartService.clearCart(userId);
        return ResponseEntity.noContent().build();
    }

    @PutMapping("/{userId}/item/{cartItemId}")
    public ResponseEntity<CartDTO> updateCartItemQuantity(
            @PathVariable("userId") Integer userId,
            @PathVariable("cartItemId") Integer cartItemId,
            @RequestParam("quantity") Integer quantity) {
        return cartService.updateCartItemQuantity(userId, cartItemId, quantity);
    }

    @PutMapping("/{userId}/apply-voucher")
    public ResponseEntity<CartDTO> applyVoucher(@PathVariable("userId") Integer userId,
            @RequestParam("voucherCode") String voucherCode) {
        try {
            CartDTO updatedCart = cartService.applyVoucher(userId, voucherCode);
            return ResponseEntity.ok(updatedCart);
        } catch (Exception e) {
            return ResponseEntity.badRequest().build();
        }
    }
}
