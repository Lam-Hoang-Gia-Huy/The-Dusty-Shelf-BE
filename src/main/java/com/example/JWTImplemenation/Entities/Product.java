package com.example.JWTImplemenation.Entities;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

import java.sql.Timestamp;
import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@EntityListeners(AuditingEntityListener.class)
@Entity
@Table
public class Product {
    @Id
    @GeneratedValue
    private Integer id;
    private String name;
    private String author;
    @ManyToOne
    @JoinColumn(name = "category_id")
    private Category category;
    @CreatedDate
    private Timestamp createdDate;
    private String description;
    private Integer price;
    private int stockQuantity;
    private boolean status;
    @Column(nullable = false, columnDefinition = "float default 0.0")
    private double averageScore;
    @OneToMany(mappedBy = "product", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<ImageUrl> imageUrl;

    @OneToMany(mappedBy = "product", cascade = CascadeType.ALL)
    private List<CartItem> cartItems;
    @OneToMany(mappedBy = "product", cascade = CascadeType.ALL)
    private List<OrderItem> orderItems;
    @OneToMany(mappedBy = "product", cascade = CascadeType.ALL)
    private List<Feedback> feedbacks;
}
