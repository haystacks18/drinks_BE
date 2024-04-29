package com.goormfj.hanzan.controller;

import com.goormfj.hanzan.domain.ChatDTO;
import com.goormfj.hanzan.domain.ChatRoomDTO;
import com.goormfj.hanzan.service.ChatService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.messaging.handler.annotation.DestinationVariable;
import org.springframework.messaging.handler.annotation.MessageMapping;
import org.springframework.messaging.handler.annotation.SendTo;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.server.ResponseStatusException;

import java.util.List;
import java.util.Map;

@RestController
@Slf4j
@RequiredArgsConstructor
@RequestMapping("/chat")

@Controller
@CrossOrigin(originPatterns = "*", allowedHeaders = "*")
public class ChatController {

    private final ChatService chatService;

    @PostMapping("/createRoom")
    public ResponseEntity<?> createRoom(@RequestBody Map<String, String> payload) {
        String roomName = payload.get("name");
        return chatService.createRoom(roomName);
    }

    @PostMapping("/joinRoomByName")
    public ResponseEntity<?> joinRoomByName(@RequestBody Map<String, String> payload) {
        String roomName = payload.get("roomName");
        String userId = payload.get("userId");
        return chatService.joinRoomByName(roomName, userId);
    }

    @MessageMapping("/chat.sendMessage/{roomId}")
    @SendTo("/topic/{roomId}")
    public ChatDTO sendMessage(@DestinationVariable String roomId, ChatDTO chatMessage) {
        ChatDTO savedChatDTO =  chatService.processMessage(chatMessage);
        if (savedChatDTO != null) {
            return savedChatDTO; // timestamp 달린 chatDTO로 broadcast
        } else {
            // 적절한 예외 처리 또는 오류 메시지 반환
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, "Room not found");
        }
    }

    @GetMapping("/roomsByUser/{userId}")
    public ResponseEntity<List<ChatRoomDTO>> getRoomsByUserId(@PathVariable String userId) {
        List<ChatRoomDTO> chatRooms = chatService.getRoomsByUserId(userId);
        return ResponseEntity.ok(chatRooms);
    }

    @GetMapping("/chatHistory/{roomId}")
    public ResponseEntity<List<ChatDTO>> getChatHistory(@PathVariable String roomId) {
        List<ChatDTO> chatHistory = chatService.getChatHistory(roomId);
        return ResponseEntity.ok(chatHistory);
    }

    @GetMapping("/searchChat/{roomId}")
    public ResponseEntity<List<ChatDTO>> searchChatMessages(
            @PathVariable String roomId,
            @RequestParam String keyword) {
        List<ChatDTO> chatMessages = chatService.searchChatMessages(roomId, keyword);
        return ResponseEntity.ok(chatMessages);
    }

}
