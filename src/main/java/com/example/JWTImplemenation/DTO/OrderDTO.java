package com.example.JWTImplemenation.DTO;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.sql.Timestamp;
import java.util.List;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class OrderDTO {
    private Integer id;
    private Integer userId;
    private String userName;
    private List<OrderItemDTO> orderItems;
    private Double totalAmount;
    private Double originalAmount;
    private Double discountAmount;
    private String voucherCode;
    private Timestamp createdDate;
    private Timestamp lastModifiedDate;
}
