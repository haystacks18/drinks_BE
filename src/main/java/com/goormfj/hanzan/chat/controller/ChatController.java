package com.goormfj.hanzan.chat.controller;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.goormfj.hanzan.chat.domain.ChatDTO;
import com.goormfj.hanzan.chat.domain.ChatRoomDTO;
import com.goormfj.hanzan.chat.service.ChatService;
import lombok.Data;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.messaging.handler.annotation.DestinationVariable;
import org.springframework.messaging.handler.annotation.MessageMapping;
import org.springframework.messaging.handler.annotation.SendTo;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.server.ResponseStatusException;

import java.util.*;

@RestController
@Slf4j
@RequiredArgsConstructor
@RequestMapping("/chat")

@Controller
@CrossOrigin(originPatterns = "*", allowedHeaders = "*")
public class ChatController {

    private final ChatService chatService;

    @Autowired
    private ObjectMapper objectMapper;

    @PostMapping("/createRoom")
    public ResponseEntity<?> createRoom(@RequestBody CreateRoomRequest request) {
        return chatService.createRoom(request.getName(), request.getThumbnailUrl(), request.getInitialMembers());
    }
    @Data
    public static class CreateRoomRequest {
        private String name;
        private String thumbnailUrl;
        private List<String> initialMembers;
    }

    @PostMapping("/joinRoomByName")
    public ResponseEntity<?> joinRoomByName(@RequestBody Map<String, String> payload) {
        // 사용하지 않음. joinRoom을 대신 사용
        String roomName = payload.get("roomName");
        String userId = payload.get("userId");
        return chatService.joinRoomByName(roomName, userId);
    }

    @PostMapping("/joinRoom")
    public ResponseEntity<?> joinRoom(@RequestBody Map<String, String> payload) {
        String roomId = payload.get("roomId");
        String userId = payload.get("userId");
        return chatService.joinRoom(roomId, userId);
    }

    @MessageMapping("/chat.sendMessage/{roomId}")
    @SendTo("/topic/{roomId}")
    public ChatDTO sendMessage(@DestinationVariable String roomId, ChatDTO chatMessage) {
        ChatDTO savedChatDTO =  chatService.processMessage(chatMessage);
        if (savedChatDTO != null) {
            return savedChatDTO; // timestamp가 포함된 chatDTO 반환, @SendTo를 통해 broadcast
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


    @PostMapping("/addMembers")
    public ResponseEntity<?> addMembers(@RequestBody Map<String, Object> payload) {
        String roomId = (String) payload.get("roomId");
        Object userIdsObj = payload.get("userIds");

        try {
            List<String> userIdsList = objectMapper.convertValue(userIdsObj, new TypeReference<List<String>>(){});
            Set<String> userIds = new HashSet<>(userIdsList);
            return chatService.addMembersToRoom(roomId, userIds);
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().body("userIds must be a list of strings");
        }
    }

    @PostMapping("/removeMembers")
    public ResponseEntity<?> removeMembers(@RequestBody Map<String, Object> payload) {
        String roomId = (String) payload.get("roomId");
        Object userIdsObj = payload.get("userIds");

        try {
            List<String> userIdsList = objectMapper.convertValue(userIdsObj, new TypeReference<List<String>>(){});
            Set<String> userIds = new HashSet<>(userIdsList);
            return chatService.removeMembersFromRoom(roomId, userIds);
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().body("userIds must be a list of strings");
        }
    }

    @GetMapping("/room/{roomId}/members")
    public ResponseEntity<List<String>> getRoomMembers(@PathVariable String roomId) {
        List<String> members = chatService.getRoomMembers(roomId);
        return ResponseEntity.ok(members);
    }
}
