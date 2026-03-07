package com.example.JWTImplemenation.Service;

import com.example.JWTImplemenation.DTO.CartDTO;
import com.example.JWTImplemenation.DTO.CartItemDTO;
import com.example.JWTImplemenation.DTO.ProductDTO;
import com.example.JWTImplemenation.Entities.*;
import com.example.JWTImplemenation.Repository.*;
import com.example.JWTImplemenation.Service.IService.ICartService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;


import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Service
public class CartService implements ICartService {

    @Autowired
    private CartRepository cartRepository;
    @Autowired
    private UserRepository userRepository;
    @Autowired
    private CartItemRepository cartItemRepository;
    @Autowired
    private ProductRepository productRepository;
    @Autowired
    private VoucherRepository voucherRepository;

    public CartDTO applyVoucher(Integer userId, String voucherCode) {
        Cart cart = cartRepository.findByUserId(userId).orElseThrow(() -> new IllegalArgumentException("Cart not found"));
        Voucher voucher = voucherRepository.findByCodeAndStatusTrue(voucherCode).orElseThrow(() -> new IllegalArgumentException("Invalid voucher code"));

        double totalPrice = cart.getCartItems().stream()
                .mapToDouble(item -> item.getProduct().getPrice() * item.getQuantity())
                .sum();

        if (totalPrice >= voucher.getMinimumPurchase() && voucher.getCurrentUsage() < voucher.getMaxUsage()) {
            double discountedPrice = totalPrice - voucher.getDiscountValue();
            cart.setTotalPrice(discountedPrice);
            cart.setVoucherCode(voucherCode);
            cartRepository.save(cart);

            // Convert Cart entity to CartDTO
            List<CartItemDTO> cartItemDTOs = convertToDTOList(cart.getCartItems());
            return new CartDTO(cartItemDTOs, discountedPrice, voucherCode);
        } else {
            throw new IllegalArgumentException("Voucher conditions not met");
        }
    }

    @Override
    public ResponseEntity<CartDTO> findCartByUserId(Integer userId) {
        Optional<User> user = userRepository.findById(userId);
        if (user.isPresent()) {
            Optional<Cart> cartOptional = cartRepository.findByUserId(userId);
            if (cartOptional.isPresent()) {
                Cart cart = cartOptional.get();

                // Check if the cart has a voucher
                String savedVoucherCode = cart.getVoucherCode();
                if (savedVoucherCode != null) {
                    Optional<Voucher> voucherOptional = voucherRepository.findByCode(savedVoucherCode);
                    if (voucherOptional.isPresent()) {
                        Voucher voucher = voucherOptional.get();
                        double totalPrice = cart.getCartItems().stream()
                                .mapToDouble(item -> item.getProduct().getPrice() * item.getQuantity())
                                .sum();
                        long now = System.currentTimeMillis();
                        boolean isVoucherValid = voucher.isStatus() &&
                                totalPrice >= voucher.getMinimumPurchase() &&
                                voucher.getMaxUsage() > voucher.getCurrentUsage() &&
                                (voucher.getStartDate() == null || voucher.getStartDate().getTime() <= now) &&
                                (voucher.getEndDate() == null || voucher.getEndDate().getTime() >= now);

                        if (!isVoucherValid) {
                            // Voucher no longer valid: reset cart in DB but keep local variable for response
                            cart.setVoucherCode(null);
                            cart.setTotalPrice(totalPrice);
                            cartRepository.save(cart);
                            savedVoucherCode = null; // Also clear local var so CartDTO won't show invalid voucher
                        }
                    } else {
                        // Voucher not found
                        cart.setVoucherCode(null);
                        cartRepository.save(cart);
                        savedVoucherCode = null;
                    }
                }

                List<CartItem> cartItems = cart.getCartItems();
                CartDTO cartDTO = new CartDTO();
                cartDTO.setCartItems(convertToDTOList(cartItems));
                cartDTO.setTotalPrice(cart.getTotalPrice());
                cartDTO.setVoucherCode(savedVoucherCode);
                return ResponseEntity.ok(cartDTO);
            } else {
                // Create a new cart if not present
                Cart newCart = new Cart();
                newCart.setUser(user.get());
                newCart.setCartItems(new ArrayList<>());
                cartRepository.save(newCart);
                CartDTO cartDTO = new CartDTO();
                cartDTO.setCartItems(new ArrayList<>());
                cartDTO.setVoucherCode(null); // No voucher code for new cart
                cartDTO.setTotalPrice(0.0);
                return ResponseEntity.ok(cartDTO);
            }
        } else {
            return ResponseEntity.notFound().build();
        }
    }

    @Override
    public ResponseEntity<CartItemDTO> addToCart(Integer userId, CartItemDTO cartItemRequest) {
        Optional<User> userOptional = userRepository.findById(userId);
        Optional<Product> productOptional = productRepository.findById(cartItemRequest.getProduct().getId());

        if (userOptional.isPresent() && productOptional.isPresent()) {
            User user = userOptional.get();
            Product product = productOptional.get();

            // Ensure the user has a cart
            Cart cart = user.getCart();
            if (cart == null) {
                cart = new Cart();
                cart.setUser(user);
                cart.setCartItems(new ArrayList<>());
                cart.setTotalPrice(0.0);
                cartRepository.save(cart);
            }

            // Check if the product is already in the cart
            Optional<CartItem> existingCartItem = cart.getCartItems().stream()
                    .filter(item -> item.getProduct().getId().equals(product.getId()))
                    .findFirst();

            CartItem cartItem;
            int requestedQuantity = cartItemRequest.getQuantity() != null && cartItemRequest.getQuantity() > 0
                    ? cartItemRequest.getQuantity() : 1;

            if (existingCartItem.isPresent()) {
                // Product is already in the cart, add the requested quantity
                cartItem = existingCartItem.get();
                cartItem.setQuantity(cartItem.getQuantity() + requestedQuantity);
            } else {
                // Add new product to the cart with requested quantity
                cartItem = new CartItem();
                cartItem.setCart(cart);
                cartItem.setProduct(product);
                cartItem.setQuantity(requestedQuantity);

                cart.getCartItems().add(cartItem);
            }

            cartItemRepository.save(cartItem);

            // Recalculate the total price of the cart
            double totalPrice = calculateCartTotal(cart);
            cart.setTotalPrice(totalPrice);
            cartRepository.save(cart);

            // Convert and return DTO
            CartItemDTO responseDTO = convertToDTO(cartItem);
            return ResponseEntity.ok(responseDTO);
        }

        return ResponseEntity.notFound().build();
    }




    @Override
    public ResponseEntity<Void> removeFromCart(Integer userId, Integer cartItemId) {
        Optional<CartItem> cartItemOptional = cartItemRepository.findById(cartItemId);
        if (cartItemOptional.isPresent() && cartItemOptional.get().getCart().getUser().getId().equals(userId)) {
            CartItem cartItem = cartItemOptional.get();
            Cart cart = cartItem.getCart();
            cartItemRepository.delete(cartItem);

            // Recalculate the total price of the cart
            double totalPrice = calculateCartTotal(cart);
            cart.setTotalPrice(totalPrice);
            cartRepository.save(cart);

            return ResponseEntity.noContent().build();
        } else {
            return ResponseEntity.notFound().build();
        }
    }


    @Override
    public ResponseEntity<CartDTO> updateCartItemQuantity(Integer userId, Integer cartItemId, Integer quantity) {
        Optional<CartItem> cartItemOptional = cartItemRepository.findById(cartItemId);
        if (cartItemOptional.isPresent() && cartItemOptional.get().getCart().getUser().getId().equals(userId)) {
            CartItem cartItem = cartItemOptional.get();
            Cart cart = cartItem.getCart();

            if (quantity <= 0) {
                cartItemRepository.delete(cartItem);
            } else {
                cartItem.setQuantity(quantity);
                cartItemRepository.save(cartItem);
            }

            double totalPrice = calculateCartTotal(cart);
            cart.setTotalPrice(totalPrice);
            cartRepository.save(cart);

            return findCartByUserId(userId);
        }
        return ResponseEntity.notFound().build();
    }

    private double calculateCartTotal(Cart cart) {
        double rawTotal = cart.getCartItems().stream()
                .mapToDouble(item -> item.getProduct().getPrice() * item.getQuantity())
                .sum();

        if (cart.getVoucherCode() != null) {
            Optional<Voucher> voucherOpt = voucherRepository.findByCode(cart.getVoucherCode());
            if (voucherOpt.isPresent()) {
                Voucher voucher = voucherOpt.get();
                // Check if still valid using Timestamp to avoid timezone issues
                long now = System.currentTimeMillis();
                if (voucher.isStatus() && 
                    rawTotal >= voucher.getMinimumPurchase() &&
                    (voucher.getStartDate() == null || voucher.getStartDate().getTime() <= now) &&
                    (voucher.getEndDate() == null || voucher.getEndDate().getTime() >= now) &&
                    voucher.getMaxUsage() > voucher.getCurrentUsage()) {
                    return rawTotal - voucher.getDiscountValue();
                } else {
                    cart.setVoucherCode(null); // Invalidated
                }
            } else {
                cart.setVoucherCode(null);
            }
        }
        return rawTotal;
    }

    private CartItemDTO convertToDTO(CartItem cartItem) {
        CartItemDTO cartItemDTO = new CartItemDTO();
        cartItemDTO.setId(cartItem.getId());
        cartItemDTO.setQuantity(cartItem.getQuantity());
        Product product = cartItem.getProduct();
        if (product != null) {
            ProductDTO productDTO = new ProductDTO();
            productDTO.setId(product.getId());
            productDTO.setName(product.getName());
            if (product.getCategory() != null) {
                productDTO.setCategory(product.getCategory().getName());
                productDTO.setCategoryId(product.getCategory().getId());
            }
            productDTO.setDescription(product.getDescription());
            productDTO.setStockQuantity(product.getStockQuantity());
            productDTO.setStatus(product.isStatus());
            productDTO.setPrice(product.getPrice());
            productDTO.setCreatedDate(product.getCreatedDate());
            productDTO.setImageUrl(product.getImageUrl().stream().map(image -> image.getImageUrl()).collect(Collectors.toList()));
            cartItemDTO.setProduct(productDTO);
        }

        return cartItemDTO;
    }

    private List<CartItemDTO> convertToDTOList(List<CartItem> cartItems) {
        return cartItems.stream().map(this::convertToDTO).collect(Collectors.toList());
    }

    @Transactional
    @Override
    public void clearCart(Integer userId) {
        Optional<Cart> cartOptional = cartRepository.findByUserId(userId);
        cartOptional.ifPresent(cart -> {
            List<CartItem> cartItems = cart.getCartItems();
            cartItemRepository.deleteAll(cartItems);
            cart.setCartItems(new ArrayList<>());
cart.setVoucherCode(null);
            // Set the total price to zero after clearing the cart
            cart.setTotalPrice(0.0);
            cartRepository.save(cart);
        });
    }

    @Override
    public List<Integer> findWatchIdsInCart(Integer userId) {
        Optional<User> userOptional = userRepository.findById(userId);
        if (userOptional.isPresent()) {
            Optional<Cart> cartOptional = cartRepository.findByUserId(userId);
            if (cartOptional.isPresent()) {
                List<CartItem> cartItems = cartOptional.get().getCartItems();
                return cartItems.stream()
                        .map(cartItem -> cartItem.getProduct().getId())
                        .collect(Collectors.toList());
            }
        }
        return new ArrayList<>(); // Return an empty list if cart or user not found
    }}