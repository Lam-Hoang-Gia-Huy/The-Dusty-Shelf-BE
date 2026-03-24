package com.example.JWTImplemenation.Service;

import org.springframework.ai.chat.client.ChatClient;
import org.springframework.ai.chat.client.advisor.MessageChatMemoryAdvisor;
import org.springframework.ai.chat.memory.ChatMemory;
import org.springframework.ai.chat.memory.MessageWindowChatMemory;
import org.springframework.stereotype.Service;

@Service
public class AiChatService {

    private final ChatClient chatClient;

    public AiChatService(ChatClient.Builder chatClientBuilder) {
        ChatMemory memory = MessageWindowChatMemory.builder().maxMessages(20).build();
        this.chatClient = chatClientBuilder
                .defaultAdvisors(MessageChatMemoryAdvisor.builder(memory).build())
                .build();
    }

    public String chatWithAi(String userMessage, String sessionId, Integer userId) {
        String systemPrompt = """
                Bạn là một trợ lý ảo tận tâm và chuyên nghiệp chăm sóc khách hàng cho cửa hàng sách (Bookstore).
                Nhiệm vụ của bạn là tư vấn các cuốn sách phù hợp với yêu cầu của khách hàng.

                QUY TẮC QUAN TRỌNG:
                1. GỌI TOOL NGAY LẬP TỨC: Khi khách hàng đưa ra bất kỳ tiêu chí nào (giá, tên, tác giả, danh mục), gọi `searchBooks`. Đừng hỏi lại nếu đủ thông tin tìm kiếm.
                2. HIỂN THỊ KẾT QUẢ TRƯỚC: Sau khi gọi tool, hãy liệt kê các sản phẩm tìm thấy.
                3. ĐỊNH DẠNG Tiền Tệ: Luôn định dạng số tiền (ví dụ: 150.000 VNĐ).
                4. NGỮ CẢNH CỬA HÀNG: Chỉ tư vấn sách có trong Database. Không thấy thì gợi ý bằng `listCategories`.
                5. CHI TIẾT SẢN PHẨM & QUẢN LÝ GIỎ HÀNG:
                   - Nếu người dùng cần thêm thông tin chi tiết của 1 sách cụ thể, gọi `getProductDetail` để lấy ID và mô tả dài.
                   - CHI CHO PHÉP thao tác với giỏ hàng nếu người dùng ĐÃ ĐĂNG NHẬP (userId != null). Nếu chưa, hãy yêu cầu đăng nhập.
                   - Xem giỏ hàng: `viewCart`.
                   - Thêm sách: `addToCart(productId, quantity)`.
                   - Cập nhật số lượng: `updateCartItemQuantity(productId, quantity)`. Nếu quantity = 0, sản phẩm sẽ bị xóa.
                   - Xóa toàn bộ giỏ hàng: `clearCart`.
                   - Sau mỗi thao tác thay đổi giỏ hàng thành công, hãy LUÔN gọi `viewCart` (ngầm hoặc hiển thị kết quả) để liệt kê lại danh sách sản phẩm hiện có bằng thẻ BOOK_CARD và hiển thị tổng tiền.
                6. ĐỊNH DẠNG TRẢ LỜI VỚI THẺ SẢN PHẨM (BOOK CARD):
                   - Khi bạn liệt kê sách (kể cả trong danh mục tìm kiếm hay TRONG GIỎ HÀNG), BẮT BUỘC chèn đoạn mã sau ở một dòng riêng biệt cho MỖI cuốn sách:
                   `[BOOK_CARD: {"id": <id>, "name": "<tên>", "price": <giá>, "imageUrl": "<url>", "quantity": <số lượng nếu có, mặc định 0>}]`
                   - Đối với giỏ hàng, hãy liệt kê từng chi tiết sản phẩm bằng thẻ BOOK_CARD này để giao diện hiển thị đẹp mắt.
                   - Không thay đổi cấu trúc của dấu ngoặc và chuỗi JSON bên trong.
                """;

        try {
            if (userId != null) {
                com.example.JWTImplemenation.Config.UserContextHolder.setUserId(userId);
            }
            return chatClient.prompt()
                    .system(systemPrompt)
                    .user(userMessage)
                    .advisors(a -> a.param("chat_memory_conversation_id", sessionId))
                    .toolNames("searchBooks", "listCategories", "getCategoryInfo", "getProductDetail", "addToCart",
                            "viewCart", "updateCartItemQuantity", "clearCart")
                    .call()
                    .content();
        } finally {
            com.example.JWTImplemenation.Config.UserContextHolder.clear();
        }
    }
}
