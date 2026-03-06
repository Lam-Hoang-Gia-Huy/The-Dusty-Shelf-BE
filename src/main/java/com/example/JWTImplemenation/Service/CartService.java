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
                if (cart.getVoucherCode() != null) {
                    Optional<Voucher> voucherOptional = voucherRepository.findByCode(cart.getVoucherCode());
                    if (voucherOptional.isPresent()) {
                        Voucher voucher = voucherOptional.get();
                        double totalPrice = cart.getCartItems().stream()
                                .mapToDouble(item -> item.getProduct().getPrice() * item.getQuantity())
                                .sum();
                        // Convert java.sql.Timestamp to java.time.LocalDate
                        LocalDate startDate = voucher.getStartDate().toLocalDateTime().toLocalDate();
                        LocalDate endDate = voucher.getEndDate().toLocalDateTime().toLocalDate();

                        // Check if the voucher is valid
                        boolean isVoucherValid = voucher.isStatus() &&
                                totalPrice >= voucher.getMinimumPurchase() &&
                                voucher.getMaxUsage() > voucher.getCurrentUsage() &&
                                LocalDate.now().isAfter(startDate) &&
                                LocalDate.now().isBefore(endDate);

                        if (!isVoucherValid) {
                            // Voucher is not available, remove the voucher from the cart
                            cart.setVoucherCode(null);
                            cart.setTotalPrice(totalPrice);
                            cartRepository.save(cart);
                        }
                    } else {
                        cart.setVoucherCode(null);
                    }
                }

                List<CartItem> cartItems = cart.getCartItems();
                CartDTO cartDTO = new CartDTO();
                cartDTO.setCartItems(convertToDTOList(cartItems));
                cartDTO.setTotalPrice(cart.getTotalPrice());
                cartDTO.setVoucherCode(cart.getVoucherCode()); // Set the voucher code
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
            if (existingCartItem.isPresent()) {
                // Product is already in the cart, update the quantity
                cartItem = existingCartItem.get();
                cartItem.setQuantity(cartItem.getQuantity() +1); // Update quantity
            } else {
                // Add new product to the cart
                cartItem = new CartItem();
                cartItem.setCart(cart);
                cartItem.setProduct(product);

                cart.getCartItems().add(cartItem);
            }

            cartItemRepository.save(cartItem);

            // Recalculate the total price of the cart
            double totalPrice = cart.getCartItems().stream()
                    .mapToDouble(item -> item.getProduct().getPrice() * item.getQuantity())
                    .sum();
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
            double totalPrice = cart.getCartItems().stream()
                    .mapToDouble(item -> item.getProduct().getPrice() * item.getQuantity())
                    .sum();

            // Check if the cart has a voucher
            if (cart.getVoucherCode() != null) {
                Optional<Voucher> voucherOptional = voucherRepository.findByCode(cart.getVoucherCode());
                if (voucherOptional.isPresent()) {
                    Voucher voucher = voucherOptional.get();
                    // Convert java.sql.Timestamp to java.time.LocalDate
                    LocalDate startDate = voucher.getStartDate().toLocalDateTime().toLocalDate();
                    LocalDate endDate = voucher.getEndDate().toLocalDateTime().toLocalDate();

                    // Check if the voucher is valid
                    boolean isVoucherValid = voucher.isStatus() &&
                            totalPrice >= voucher.getMinimumPurchase() &&
                            voucher.getMaxUsage() > voucher.getCurrentUsage() &&
                            LocalDate.now().isAfter(startDate) &&
                            LocalDate.now().isBefore(endDate);

                    if (!isVoucherValid) {
                        // Voucher is not available, remove the voucher from the cart
                        cart.setVoucherCode(null);
                    }
                } else {
                    cart.setVoucherCode(null);
                }
            }

            cart.setTotalPrice(totalPrice);
            cartRepository.save(cart);

            return ResponseEntity.noContent().build();
        } else {
            return ResponseEntity.notFound().build();
        }
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