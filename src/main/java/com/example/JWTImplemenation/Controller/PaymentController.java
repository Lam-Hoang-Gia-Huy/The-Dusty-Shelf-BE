package com.example.JWTImplemenation.Controller;

import com.example.JWTImplemenation.Config.VNPayConfig;
import com.example.JWTImplemenation.DTO.CartDTO;
import com.example.JWTImplemenation.DTO.CartItemDTO;
import com.example.JWTImplemenation.DTO.OrderDTO;
import com.example.JWTImplemenation.DTO.OrderItemDTO;
import com.example.JWTImplemenation.Entities.CartItem;
import com.example.JWTImplemenation.Entities.Product;
import com.example.JWTImplemenation.Entities.Voucher;
import com.example.JWTImplemenation.Repository.ProductRepository;
import com.example.JWTImplemenation.Service.IService.ICartService;
import com.example.JWTImplemenation.Service.IService.IOrderService;
import com.example.JWTImplemenation.Service.IService.IVoucherService;
import org.apache.commons.codec.digest.HmacAlgorithms;
import org.apache.commons.codec.digest.HmacUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.*;

import java.io.UnsupportedEncodingException;
import java.net.InetAddress;
import java.net.URLEncoder;
import java.net.UnknownHostException;
import java.nio.charset.StandardCharsets;
import java.text.SimpleDateFormat;
import java.util.*;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/v1/payment")
public class PaymentController {
    @Autowired
    private ProductRepository productRepository;
    @Autowired
    private ICartService cartService;
    @Autowired
    private IVoucherService voucherService;
    @Autowired
    private IOrderService orderService;

    @PostMapping("/create-payment-url")
    public Map<String, String> createPaymentUrl(@RequestBody Map<String, Object> payload) throws UnknownHostException, UnsupportedEncodingException {
        String vnp_IpAddr = InetAddress.getLocalHost().getHostAddress();
        int amount = (int) payload.get("amount");
        String orderInfo = (String) payload.get("orderInfo");
        String vnp_Version = VNPayConfig.VNPAY_VERSION;
        String vnp_Command = VNPayConfig.VNPAY_COMMAND;
        String vnp_TmnCode = VNPayConfig.VNPAY_TMNCODE;
        String vnp_HashSecret = VNPayConfig.VNPAY_HASH_SECRET;
        String vnp_ReturnUrl = VNPayConfig.VNPAY_RETURNURL;

        SimpleDateFormat formatter = new SimpleDateFormat("yyyyMMddHHmmss");
        Calendar cal = Calendar.getInstance(TimeZone.getTimeZone("Etc/GMT+7"));

        String vnp_TxnRef = String.valueOf(System.currentTimeMillis());
        String vnp_OrderInfo = orderInfo;
        String orderType = "other";
        String vnp_Amount = String.valueOf(amount * 100);
        String vnp_Locale = "vn";
        String vnp_CreateDate = formatter.format(cal.getTime());

        Map<String, String> vnp_Params = new HashMap<>();
        vnp_Params.put("vnp_Version", vnp_Version);
        vnp_Params.put("vnp_Command", vnp_Command);
        vnp_Params.put("vnp_TmnCode", vnp_TmnCode);
        vnp_Params.put("vnp_Amount", vnp_Amount);
        vnp_Params.put("vnp_CurrCode", "VND");
        vnp_Params.put("vnp_TxnRef", vnp_TxnRef);
        vnp_Params.put("vnp_OrderInfo", vnp_OrderInfo);
        vnp_Params.put("vnp_OrderType", orderType);
        vnp_Params.put("vnp_Locale", vnp_Locale);
        vnp_Params.put("vnp_ReturnUrl", vnp_ReturnUrl);
        vnp_Params.put("vnp_IpAddr", vnp_IpAddr);
        vnp_Params.put("vnp_CreateDate", vnp_CreateDate);

        List<String> fieldNames = new ArrayList<>(vnp_Params.keySet());
        Collections.sort(fieldNames);
        StringBuilder hashData = new StringBuilder();
        StringBuilder query = new StringBuilder();
        for (String fieldName : fieldNames) {
            String fieldValue = vnp_Params.get(fieldName);
            if ((fieldValue != null) && (fieldValue.length() > 0)) {
                hashData.append(fieldName);
                hashData.append('=');
                hashData.append(URLEncoder.encode(fieldValue, StandardCharsets.US_ASCII.toString()));
                hashData.append('&');
                query.append(URLEncoder.encode(fieldName, StandardCharsets.US_ASCII.toString()));
                query.append('=');
                query.append(URLEncoder.encode(fieldValue, StandardCharsets.US_ASCII.toString()));
                query.append('&');
            }
        }
        hashData.setLength(hashData.length() - 1);
        query.setLength(query.length() - 1);

        String vnp_SecureHash = new HmacUtils(HmacAlgorithms.HMAC_SHA_512, vnp_HashSecret).hmacHex(hashData.toString());
        query.append("&vnp_SecureHash=").append(URLEncoder.encode(vnp_SecureHash, StandardCharsets.US_ASCII.toString()));
        String paymentUrl = VNPayConfig.VNPAY_URL + "?" + query.toString();

        Map<String, String> response = new HashMap<>();
        response.put("paymentUrl", paymentUrl);
        return response;
    }

    @PostMapping("/verify-payment/{id}")
    public Map<String, Object> verifyPayment(@PathVariable Integer id, @RequestBody Map<String, String> payload) {
        String vnp_HashSecret = VNPayConfig.VNPAY_HASH_SECRET;
        String secureHash = payload.remove("vnp_SecureHash");

        Map<String, Object> response = new HashMap<>();

        if (secureHash == null || secureHash.isEmpty()) {
            response.put("success", false);
            response.put("message", "Missing secure hash.");
            return response;
        }

        List<String> fieldNames = new ArrayList<>(payload.keySet());
        Collections.sort(fieldNames);
        StringBuilder hashData = new StringBuilder();
        for (String fieldName : fieldNames) {
            String fieldValue = payload.get(fieldName);
            if ((fieldValue != null) && (fieldValue.length() > 0)) {
                try {
                    hashData.append(fieldName);
                    hashData.append('=');
                    hashData.append(URLEncoder.encode(fieldValue, StandardCharsets.US_ASCII.toString()));
                    hashData.append('&');
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        }
        if (hashData.length() > 0) {
            hashData.setLength(hashData.length() - 1);
        }

        String calculatedHash = new HmacUtils(HmacAlgorithms.HMAC_SHA_512, vnp_HashSecret).hmacHex(hashData.toString());

        boolean isSuccess = secureHash.equals(calculatedHash);
        response.put("success", isSuccess);

        if (isSuccess) {
            // Retrieve cart items for the user
            ResponseEntity<CartDTO> cartResponse = cartService.findCartByUserId(id);
            if (cartResponse != null && cartResponse.getStatusCode().is2xxSuccessful()) {
                CartDTO cartDTO = cartResponse.getBody();
                String voucherCode = cartDTO.getVoucherCode();
                double cartTotalPrice = cartDTO.getTotalPrice();

                System.out.println("[PaymentController] Cart fetched: totalPrice=" + cartTotalPrice
                        + ", voucherCode=" + voucherCode
                        + ", itemCount=" + (cartDTO.getCartItems() != null ? cartDTO.getCartItems().size() : 0));

                List<CartItemDTO> cartItemsDTO = cartDTO.getCartItems();
                List<CartItemDTO> invalidItems = cartItemsDTO.stream()
                        .filter(item -> !item.getProduct().isStatus())
                        .collect(Collectors.toList());
                if (!invalidItems.isEmpty()) {
                    response.put("success", false);
                    response.put("message", "Payment failed due to invalid items in the cart.");
                    return response;
                }

                List<CartItem> cartItems = cartItemsDTO.stream().map(dto -> {
                    Product product = productRepository.findById(dto.getProduct().getId()).orElseThrow();
                    return CartItem.builder()
                            .product(product)
                            .quantity(dto.getQuantity())
                            .build();
                }).collect(Collectors.toList());

                try {
                    // 1. Update product stock quantities
                    updateProductQuantities(cartItems);

                    // 2. Increment voucher usage
                    if (voucherCode != null) {
                        Optional<Voucher> voucher = voucherService.findByCode(voucherCode);
                        voucher.ifPresent(voucherService::incrementUsage);
                        System.out.println("[PaymentController] Voucher usage incremented for: " + voucherCode);
                    }

                    // 3. Calculate prices for the order directly here in Backend
                    double originalPrice = cartItemsDTO.stream()
                            .mapToDouble(item -> (double) item.getProduct().getPrice() * item.getQuantity())
                            .sum();
                    double discountAmount = Math.max(0, originalPrice - cartTotalPrice);

                    System.out.println("[PaymentController] Building order: userId=" + id
                            + ", original=" + originalPrice
                            + ", total=" + cartTotalPrice
                            + ", discount=" + discountAmount
                            + ", voucher=" + voucherCode);

                    // 4. Build order items from cart
                    List<OrderItemDTO> orderItemDTOs = cartItemsDTO.stream().map(item -> {
                        OrderItemDTO dto = new OrderItemDTO();
                        dto.setProduct(item.getProduct());
                        dto.setQuantity(item.getQuantity());
                        return dto;
                    }).collect(Collectors.toList());

                    // 5. Create order directly in Backend
                    OrderDTO newOrder = new OrderDTO();
                    newOrder.setUserId(id);
                    newOrder.setOrderItems(orderItemDTOs);
                    newOrder.setTotalAmount(cartTotalPrice);
                    newOrder.setOriginalAmount(originalPrice);
                    newOrder.setDiscountAmount(discountAmount);
                    newOrder.setVoucherCode(voucherCode);

                    ResponseEntity<OrderDTO> createdOrder = orderService.createOrder(newOrder);

                    if (createdOrder != null && createdOrder.getBody() != null) {
                        response.put("orderId", createdOrder.getBody().getId());
                        System.out.println("[PaymentController] Order created with ID: " + createdOrder.getBody().getId());
                    }
                    response.put("message", "Payment verified, order created");

                } catch (IllegalArgumentException e) {
                    response.put("success", false);
                    response.put("message", e.getMessage());
                    return response;
                }

            } else {
                System.err.println("Failed to retrieve cart items for user: " + id);
                response.put("success", false);
                response.put("message", "Failed to retrieve cart items.");
                return response;
            }
        } else {
            response.put("message", "Secure hash does not match.");
        }

        return response;
    }

    @Transactional
    public void updateProductQuantities(List<CartItem> cartItems) {
        for (CartItem item : cartItems) {
            Product product = item.getProduct();
            int newStock = product.getStockQuantity() - item.getQuantity();
            if (newStock < 0) {
                throw new IllegalArgumentException("Insufficient stock for product: " + product.getName());
            }
            product.setStockQuantity(newStock);
            productRepository.save(product);
        }
    }
}
