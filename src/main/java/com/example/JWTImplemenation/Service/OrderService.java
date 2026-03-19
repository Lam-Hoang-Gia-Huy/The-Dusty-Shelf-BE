package com.example.JWTImplemenation.Service;

import com.example.JWTImplemenation.DTO.OrderDTO;
import com.example.JWTImplemenation.DTO.OrderItemDTO;
import com.example.JWTImplemenation.DTO.ProductDTO;
import com.example.JWTImplemenation.Entities.*;
import com.example.JWTImplemenation.Repository.OrderRepository;
import com.example.JWTImplemenation.Repository.UserRepository;
import com.example.JWTImplemenation.Repository.ProductRepository;
import com.example.JWTImplemenation.Service.IService.IOrderService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Service
public class OrderService implements IOrderService {

    @Autowired
    private OrderRepository orderRepository;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private ProductRepository watchRepository;

    @Override
    public ResponseEntity<OrderDTO> createOrder(OrderDTO orderDTO) {
        Order order = new Order();
        User user = userRepository.findById(orderDTO.getUserId())
                .orElseThrow(() -> new RuntimeException("User not found"));
        order.setUser(user);
        order.setTotalAmount(orderDTO.getTotalAmount());
        order.setOriginalAmount(orderDTO.getOriginalAmount());
        order.setDiscountAmount(orderDTO.getDiscountAmount());
        order.setVoucherCode(orderDTO.getVoucherCode());

        System.out.println("Creating order with data: Total=" + orderDTO.getTotalAmount() +
                ", Original=" + orderDTO.getOriginalAmount() +
                ", Discount=" + orderDTO.getDiscountAmount() +
                ", Voucher=" + orderDTO.getVoucherCode());

        List<OrderItem> orderItems = orderDTO.getOrderItems().stream().map(itemDTO -> {
            OrderItem orderItem = new OrderItem();
            Product product = watchRepository.findById(itemDTO.getProduct().getId())
                    .orElseThrow(() -> new RuntimeException("Product not found"));
            orderItem.setProduct(product);
            orderItem.setPrice(itemDTO.getProduct().getPrice());
            orderItem.setQuantity(itemDTO.getQuantity()); // Set quantity
            orderItem.setOrder(order);
            return orderItem;
        }).collect(Collectors.toList());

        order.setOrderItems(orderItems);

        Order savedOrder = orderRepository.save(order);
        return ResponseEntity.ok(convertToDTO(savedOrder));
    }

    @Override
    public ResponseEntity<List<OrderDTO>> getAllOrders() {
        List<Order> orders = orderRepository.findAll();
        List<OrderDTO> orderDTOs = orders.stream()
                .map(this::convertToDTO)
                .collect(Collectors.toList());
        return ResponseEntity.ok(orderDTOs);
    }

    @Override
    public ResponseEntity<List<OrderDTO>> getOrdersByUserId(Integer userId) {
        List<Order> orders = orderRepository.findByUserId(userId);
        List<OrderDTO> orderDTOs = orders.stream()
                .map(this::convertToDTO)
                .collect(Collectors.toList());
        return ResponseEntity.ok(orderDTOs);
    }

    @Override
    public ResponseEntity<OrderDTO> getOrderById(Integer orderId) {
        Optional<Order> order = orderRepository.findById(orderId);
        return order.map(this::convertToDTO)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    private OrderDTO convertToDTO(Order order) {
        OrderDTO orderDTO = new OrderDTO();
        orderDTO.setId(order.getId());
        orderDTO.setUserId(order.getUser().getId());
        orderDTO.setTotalAmount(order.getTotalAmount());
        orderDTO.setOriginalAmount(order.getOriginalAmount());
        orderDTO.setDiscountAmount(order.getDiscountAmount());
        orderDTO.setVoucherCode(order.getVoucherCode());
        orderDTO.setUserName(order.getUser().getName());
        orderDTO.setCreatedDate(order.getCreatedDate());
        List<OrderItemDTO> orderItemDTOs = order.getOrderItems().stream()
                .map(this::convertOrderItemToDTO)
                .collect(Collectors.toList());
        orderDTO.setOrderItems(orderItemDTOs);

        return orderDTO;
    }

    private OrderItemDTO convertOrderItemToDTO(OrderItem orderItem) {
        OrderItemDTO orderItemDTO = new OrderItemDTO();
        ProductDTO productDTO = convertBookToDTO(orderItem.getProduct());
        orderItemDTO.setId(orderItem.getId());
        orderItemDTO.setProduct(productDTO);
        orderItemDTO.setQuantity(orderItem.getQuantity()); // Set quantity
        return orderItemDTO;
    }

    private ProductDTO convertBookToDTO(Product product) {
        ProductDTO productDTO = new ProductDTO();
        productDTO.setId(product.getId());
        productDTO.setName(product.getName());
        if (product.getCategory() != null) {
            productDTO.setCategory(product.getCategory().getName());
            productDTO.setCategoryId(product.getCategory().getId());
        }
        productDTO.setDescription(product.getDescription());
        productDTO.setPrice(product.getPrice());
        productDTO.setStockQuantity(product.getStockQuantity());
        productDTO.setStatus(product.isStatus());
        productDTO.setCreatedDate(product.getCreatedDate());
        if (product.getImageUrl() != null) {
            List<String> imageUrls = product.getImageUrl()
                    .stream()
                    .map(ImageUrl::getImageUrl)
                    .collect(Collectors.toList());
            productDTO.setImageUrl(imageUrls);
        }
        return productDTO;
    }
}
