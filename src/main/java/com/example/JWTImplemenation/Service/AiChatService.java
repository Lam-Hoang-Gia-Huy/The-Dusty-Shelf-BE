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
                5. CHI TIẾT SẢN PHẨM & THÊM VÀO GIỎ:
                   - Nếu người dùng cần thêm thông tin chi tiết của 1 sách cụ thể, gọi `getProductDetail` để lấy ID và mô tả dài.
                   - CHI CHO PHÉP thêm vào giỏ hàng nếu người dùng ĐÃ ĐĂNG NHẬP (cửa hàng ghi nhận được userId). Nếu chưa đăng nhập, TUYỆT ĐỐI KHÔNG gọi tool mà hãy lịch sự yêu cầu người dùng đăng nhập tài khoản trên website.
                   - Nếu người dùng muốn mua hoặc thêm vào giỏ hàng, hãy gọi `addToCart(productId, quantity)`. 
                   - Sau khi thêm vào giỏ hàng thành công, hãy liệt kê danh sách các sản phẩm đang có trong giỏ hàng và tổng tiền dựa trên thông tin tool trả về.
                6. ĐỊNH DẠNG TRẢ VỜI VỚI THẺ SẢN PHẨM (BOOK CARD):
                   - Khi bạn liệt kê sách, BẮT BUỘC chèn đoạn mã sau ở một dòng riêng biệt cho MỖI cuốn sách (giúp UI hiển thị thẻ sản phẩm):
                   `[BOOK_CARD: {"id": <id>, "name": "<tên>", "price": <giá>, "imageUrl": "<url>"}]`
                   - Không thay đổi cấu trúc của dấu ngoặc và chuỗi JSON bên trong.
                   - Bạn vẫn có thể dùng Markdown bình thường để nói chuyện.
                """;

        try {
            if (userId != null) {
                com.example.JWTImplemenation.Config.UserContextHolder.setUserId(userId);
            }
            return chatClient.prompt()
                    .system(systemPrompt)
                    .user(userMessage)
                    .advisors(a -> a.param("chat_memory_conversation_id", sessionId))
                    .toolNames("searchBooks", "listCategories", "getCategoryInfo", "getProductDetail", "addToCart")
                    .call()
                    .content();
        } finally {
            com.example.JWTImplemenation.Config.UserContextHolder.clear();
        }
    }
}
