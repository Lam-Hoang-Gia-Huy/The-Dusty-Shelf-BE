package com.example.JWTImplemenation.Controller;

import com.example.JWTImplemenation.Service.AiChatService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.Map;
import java.util.UUID;
import java.util.HashMap;

@RestController
@RequestMapping("/api/ai")
@RequiredArgsConstructor
public class AiChatController {

    private final AiChatService aiChatService;

    @PostMapping("/chat")
    public ResponseEntity<Map<String, String>> chatWithAi(@RequestBody Map<String, String> request) {
        String userMessage = request.get("message");
        if (userMessage == null || userMessage.trim().isEmpty()) {
            return ResponseEntity.badRequest().body(Map.of("error", "Message is required"));
        }

        String sessionId = request.get("sessionId");
        if (sessionId == null || sessionId.trim().isEmpty()) {
            sessionId = UUID.randomUUID().toString();
        }

        String userIdStr = request.get("userId");
        Integer userId = null;
        if (userIdStr != null && !userIdStr.toString().trim().isEmpty()) {
            try {
                userId = Integer.parseInt(userIdStr.toString());
            } catch (NumberFormatException e) {
                // Ignore parse error
            }
        }

        String aiResponse = aiChatService.chatWithAi(userMessage, sessionId, userId);

        Map<String, String> response = new HashMap<>();
        response.put("response", aiResponse);
        response.put("sessionId", sessionId);
        return ResponseEntity.ok(response);
    }
}
