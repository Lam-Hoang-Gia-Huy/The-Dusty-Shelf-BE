package com.example.JWTImplemenation.Service;

import org.springframework.ai.chat.client.ChatClient;
import org.springframework.stereotype.Service;

@Service
public class AiChatService {

    private final ChatClient chatClient;

    public AiChatService(ChatClient.Builder chatClientBuilder) {
        this.chatClient = chatClientBuilder.build();
    }

    public String chatWithAi(String userMessage) {
        String systemPrompt = """
                Bạn là một trợ lý ảo tận tâm và chuyên nghiệp chăm sóc khách hàng cho cửa hàng sách (Bookstore).
                Nhiệm vụ của bạn là tư vấn các cuốn sách phù hợp với yêu cầu của khách hàng.

                QUY TẮC QUAN TRỌNG:
                1. GỌI TOOL NGAY LẬP TỨC: Khi khách hàng đưa ra bất kỳ tiêu chí nào (giá, tên, tác giả, danh mục), hãy gọi `searchBooks` ngay lập tức. ĐỪNG hỏi lại để xác nhận nếu thông tin đã đủ để tìm kiếm sơ bộ.
                2. HIỂN THỊ KẾT QUẢ TRƯỚC: Sau khi gọi tool, hãy liệt kê các sản phẩm tìm thấy. Chỉ đặt câu hỏi gợi ý sau khi đã đưa ra kết quả.
                3. ĐỊNH DẠNG TIỀN TỆ: Luôn định dạng số tiền (ví dụ: 150.000 VNĐ).
                4. NGỮ CẢNH CỬA HÀNG: Chỉ tư vấn sách có trong Database. Nếu không thấy, hãy gợi ý danh mục liên quan bằng `listCategories`.

                Ví dụ: "Tìm sách dưới 300k" -> Gọi `searchBooks(maxPrice=300000)` rồi trả lời kết quả.
                """;

        return chatClient.prompt()
                .system(systemPrompt)
                .user(userMessage)
                .toolNames("searchBooks", "listCategories", "getCategoryInfo")
                .call()
                .content();
    }
}
