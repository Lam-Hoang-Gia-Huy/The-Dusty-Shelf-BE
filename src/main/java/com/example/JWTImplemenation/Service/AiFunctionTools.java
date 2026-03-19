package com.example.JWTImplemenation.Service;

import com.example.JWTImplemenation.Entities.Product;
import com.example.JWTImplemenation.Repository.ProductRepository;
import com.example.JWTImplemenation.Repository.CategoryRepository;
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
    private final CategoryRepository categoryRepository;

    public record BookSearchRequest(String name, String author, String category, Integer minPrice, Integer maxPrice,
            Boolean inStockOnly, Double minScore) {
    }

    public record CategoryInfoRequest(String category) {
    }

    public record EmptyRequest() {
    }

    public record BookSearchResult(List<BookInfo> books) {
    }

    public record BookInfo(String name, String author, Integer price, String description, int stockQuantity) {
        public static BookInfo fromProduct(Product p) {
            return new BookInfo(p.getName(), p.getAuthor(), p.getPrice(), p.getDescription(), p.getStockQuantity());
        }
    }

    public record CategoryInfo(String name, String description, List<BookInfo> topBooks) {
    }

    public record CategoryListResult(List<CategorySummary> categories) {
    }

    public record CategorySummary(String name, String description) {
    }

    @Bean
    @Description("Tìm kiếm sách theo tên, tác giả, danh mục, khoảng giá, tình trạng còn hàng và điểm đánh giá tối thiểu")
    public Function<BookSearchRequest, BookSearchResult> searchBooks() {
        return request -> {
            log.info("AI Function Called - searchBooks: {}", request);

            Integer minStock = (request.inStockOnly() != null && request.inStockOnly()) ? 1 : null;

            Page<Product> page = productRepository.searchBooksCustom(
                    request.name(),
                    request.author(),
                    request.category(),
                    request.minPrice(),
                    request.maxPrice(),
                    true, // Only active products
                    minStock,
                    request.minScore(),
                    PageRequest.of(0, 5));

            List<BookInfo> books = page.getContent().stream()
                    .map(BookInfo::fromProduct)
                    .collect(Collectors.toList());

            return new BookSearchResult(books);
        };
    }

    @Bean
    @Description("Liệt kê tất cả các danh mục sách có sẵn trong cửa hàng")
    public Function<EmptyRequest, CategoryListResult> listCategories() {
        return (request) -> {
            log.info("AI Function Called - listCategories");
            List<CategorySummary> categories = categoryRepository.findAll().stream()
                    .map(c -> new CategorySummary(c.getName(), c.getDescription()))
                    .collect(Collectors.toList());
            log.info("Returning {} categories", categories.size());
            return new CategoryListResult(categories);
        };
    }

    @Bean
    @Description("Lấy thông tin chi tiết của một danh mục sách cụ thể bao gồm mô tả và một số sách tiêu biểu")
    public Function<CategoryInfoRequest, CategoryInfo> getCategoryInfo() {
        return request -> {
            String categoryName = request.category();
            log.info("AI Function Called - getCategoryInfo: {}", categoryName);
            return categoryRepository.findByName(categoryName)
                    .map(c -> {
                        List<BookInfo> topBooks = c.getProducts().stream()
                                .limit(3)
                                .map(BookInfo::fromProduct)
                                .collect(Collectors.toList());
                        return new CategoryInfo(c.getName(), c.getDescription(), topBooks);
                    })
                    .orElse(null);
        };
    }
}
