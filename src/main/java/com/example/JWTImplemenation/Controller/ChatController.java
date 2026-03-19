package com.example.JWTImplemenation.Controller;

import com.example.JWTImplemenation.DTO.ChatMessageDTO;
import com.example.JWTImplemenation.DTO.ChatMessageRequest;
import com.example.JWTImplemenation.DTO.ChatSessionDTO;
import com.example.JWTImplemenation.DTO.ChatStartRequest;
import com.example.JWTImplemenation.Service.IService.IChatService;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@Controller
@RequestMapping("/api/v1/chat")
public class ChatController {

    @Autowired
    private IChatService chatService;

    @PostMapping("/start")
    @ResponseBody
    public ResponseEntity<ChatSessionDTO> startChatSession(@RequestBody ChatStartRequest chatStartRequest) {
        try {
            return chatService.startChatSession(chatStartRequest);
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().body(null);
        }
    }

    @GetMapping("/{sessionId}/messages")
    @ResponseBody
    public ResponseEntity<List<ChatMessageDTO>> getChatMessages(@PathVariable("sessionId") Integer sessionId) {
        return chatService.getChatMessages(sessionId);
    }

    @PostMapping("/{sessionId}/messages")
    @ResponseBody
    public ResponseEntity<ChatMessageDTO> sendChatMessage(@PathVariable("sessionId") Integer sessionId,
            @RequestBody ChatMessageRequest chatMessageRequest) {

        return chatService.sendChatMessage(sessionId, chatMessageRequest.getMessage(),
                chatMessageRequest.getSenderId());
    }

    @GetMapping("/sessions/{userId}")
    @ResponseBody
    public ResponseEntity<List<ChatSessionDTO>> getChatSessionsByUserId(@PathVariable("userId") Integer userId) {
        return chatService.getChatSessionsByUserId(userId);
    }
}
