package com.example.JWTImplemenation.Repository;

import com.example.JWTImplemenation.Entities.Product;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

public interface ProductRepository extends JpaRepository<Product, Integer> {
        @Query("SELECT w FROM Product w LEFT JOIN w.category c WHERE " +
                        "(?1 IS NULL OR ?1 = '' OR LOWER(w.name) LIKE LOWER(CONCAT('%', ?1, '%'))) AND " +
                        "(?2 IS NULL OR ?2 = '' OR LOWER(w.author) LIKE LOWER(CONCAT('%', ?2, '%'))) AND " +
                        "(?3 IS NULL OR ?3 = '' OR LOWER(c.name) LIKE LOWER(CONCAT('%', ?3, '%'))) AND " +
                        "(?4 IS NULL OR w.price >= ?4) AND " +
                        "(?5 IS NULL OR w.price <= ?5) AND " +
                        "(?6 IS NULL OR w.status = ?6) AND " +
                        "(?7 IS NULL OR w.stockQuantity >= ?7) AND " +
                        "(?8 IS NULL OR w.averageScore >= ?8)")
        Page<Product> searchBooksCustom(
                        String name,
                        String author,
                        String category,
                        Integer minPrice,
                        Integer maxPrice,
                        Boolean status,
                        Integer minStock,
                        Double minScore,
                        Pageable pageable);
}
