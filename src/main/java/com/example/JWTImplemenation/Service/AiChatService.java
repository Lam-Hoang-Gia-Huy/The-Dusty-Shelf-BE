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

                Quy tắc:
                1. Luôn sử dụng Function Calling (Tools) để tìm kiếm sách trong Database thay vì tự bịa ra thông tin.
                2. Trả lời một cách tự nhiên, thân thiện và súc tích bằng tiếng Việt.
                3. Khi báo giá sản phẩm, hãy định dạng số tiền cho dễ đọc (ví dụ: 150.000 VNĐ thay vì 150000).
                4. Chỉ tư vấn về sách có sẵn trong cửa hàng. Nếu không tìm thấy sản phẩm phù hợp, hãy xin lỗi và có thể gợi ý sản phẩm khác.
                """;

        return chatClient.prompt()
                .system(systemPrompt)
                .user(userMessage)
                .toolNames("searchBooks")
                .call()
                .content();
    }
}
