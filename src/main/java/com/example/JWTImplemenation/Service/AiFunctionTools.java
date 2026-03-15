package com.example.JWTImplemenation.Service;

import com.example.JWTImplemenation.Entities.Product;
import com.example.JWTImplemenation.Repository.ProductRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Description;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;

import java.util.List;
import java.util.function.Function;
import java.util.stream.Collectors;

@Configuration
@RequiredArgsConstructor
@Slf4j
public class AiFunctionTools {

    private final ProductRepository productRepository;

    public record BookSearchRequest(String name, String category, Integer minPrice, Integer maxPrice) {
    }

    public record BookSearchResult(List<BookInfo> books) {
    }

    public record BookInfo(String name, Integer price, String description, int stockQuantity) {
        public static BookInfo fromProduct(Product p) {
            return new BookInfo(p.getName(), p.getPrice(), p.getDescription(), p.getStockQuantity());
        }
    }

    @Bean
    @Description("Tìm kiếm sách theo tên, danh mục (tiểu thuyết, self-help...), và khoảng giá (minPrice, maxPrice)")
    public Function<BookSearchRequest, BookSearchResult> searchBooks() {
        return request -> {
            log.info("AI Function Called - searchBooks: {}", request);

            // Limit to 5 results so AI does not get overwhelmed
            Page<Product> page = productRepository.searchWatches(
                    request.name(),
                    request.category(),
                    request.minPrice(),
                    request.maxPrice(),
                    PageRequest.of(0, 5));

            List<BookInfo> books = page.getContent().stream()
                    .filter(Product::isStatus) // Only show active products
                    .map(BookInfo::fromProduct)
                    .collect(Collectors.toList());

            return new BookSearchResult(books);
        };
    }
}
