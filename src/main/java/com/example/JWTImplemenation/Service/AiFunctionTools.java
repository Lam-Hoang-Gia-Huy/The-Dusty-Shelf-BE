package com.example.JWTImplemenation.Service;

import com.example.JWTImplemenation.Entities.Product;
import com.example.JWTImplemenation.Repository.ProductRepository;
import com.example.JWTImplemenation.Repository.CategoryRepository;
import com.example.JWTImplemenation.Service.IService.ICartService;
import com.example.JWTImplemenation.DTO.CartDTO;
import com.example.JWTImplemenation.DTO.CartItemDTO;
import com.example.JWTImplemenation.DTO.ProductDTO;
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
    private final ICartService cartService;

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

    public record ProductDetailRequest(String name) {
    }

    public record ProductDetailResult(Integer id, String name, String author, Integer price, String description,
            int stockQuantity, String imageUrl) {
        public static ProductDetailResult fromProduct(Product p) {
            String url = (p.getImageUrl() != null && !p.getImageUrl().isEmpty())
                    ? p.getImageUrl().get(0).getImageUrl()
                    : null;
            return new ProductDetailResult(p.getId(), p.getName(), p.getAuthor(), p.getPrice(), p.getDescription(),
                    p.getStockQuantity(), url);
        }
    }

    public record AddToCartRequest(Integer productId, Integer quantity) {
    }

    public record AddToCartResult(boolean success, String message, CartDTO cart) {
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

    @Bean
    @Description("Lấy thông tin chi tiết của một cuốn sách bao gồm cả id, hình ảnh, tác giả, giá tiền và mô tả dài dựa theo tên sách")
    public Function<ProductDetailRequest, ProductDetailResult> getProductDetail() {
        return request -> {
            log.info("AI Function Called - getProductDetail: {}", request.name());
            Page<Product> page = productRepository.searchBooksCustom(
                    request.name(), null, null, null, null, true, null, null, PageRequest.of(0, 1));
            return page.getContent().stream().findFirst().map(ProductDetailResult::fromProduct).orElse(null);
        };
    }

    @Bean
    @Description("Thêm một cuốn sách vào giỏ hàng của người dùng hiện tại, dựa theo productId và số lượng quantity")
    public Function<AddToCartRequest, AddToCartResult> addToCart() {
        return request -> {
            log.info("AI Function Called - addToCart: {}", request);
            Integer userId = com.example.JWTImplemenation.Config.UserContextHolder.getUserId();
            if (userId == null) {
                return new AddToCartResult(false,
                        "Từ chối thêm giỏ hàng. Vui lòng đăng nhập tài khoản trên website để sử dụng tính năng này.",
                        null);
            }
            try {
                CartItemDTO dto = new CartItemDTO();
                dto.setQuantity(request.quantity() != null && request.quantity() > 0 ? request.quantity() : 1);
                ProductDTO pdto = new ProductDTO();
                pdto.setId(request.productId());
                dto.setProduct(pdto);

                var response = cartService.addToCart(userId, dto);
                if (response.getStatusCode().is2xxSuccessful()) {
                    var cartResponse = cartService.findCartByUserId(userId);
                    return new AddToCartResult(true, "Đã thêm vào giỏ hàng thành công.", cartResponse.getBody());
                }
                return new AddToCartResult(false, "Không thể thêm vào giỏ hàng. Kiểm tra lại sản phẩm.", null);
            } catch (Exception e) {
                log.error("Error adding to cart: ", e);
                return new AddToCartResult(false, "Lỗi hệ thống khi thêm vào giỏ hàng.", null);
            }
        };
    }
}
