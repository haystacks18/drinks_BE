package com.goormfj.hanzan.service;

import com.goormfj.hanzan.domain.Chat;
import com.goormfj.hanzan.domain.ChatDTO;
import com.goormfj.hanzan.domain.ChatRoom;
import com.goormfj.hanzan.domain.ChatRoomDTO;
import com.goormfj.hanzan.repository.ChatRepository;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.goormfj.hanzan.repository.ChatRoomRepository;
import lombok.Data;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import java.util.stream.Collectors;

@Slf4j
@Data
@Service
public class ChatService {

    private final ChatRoomRepository chatRoomRepository;
    private final ObjectMapper mapper;
    private final ChatRepository chatRepository;

    @Autowired
    public ChatService(ChatRoomRepository chatRoomRepository, ChatRepository chatRepository, ObjectMapper mapper) {
        this.chatRoomRepository = chatRoomRepository;
        this.chatRepository = chatRepository;
        this.mapper = mapper;
    }

    public ChatRoomDTO convertToRoomDTO(ChatRoom chatRoom) {
        return ChatRoomDTO.builder()
                .roomId(chatRoom.getRoomId())
                .name(chatRoom.getName())
                .build();
    }

    public ChatDTO convertToChatDTO(Chat chat) {
        return ChatDTO.builder()
                .type(chat.getType())
                .message(chat.getMessage())
                .roomId(chat.getRoomId())
                .sender(chat.getSender())
                .timestamp(chat.getTimestamp())
                .build();
    }

    public Chat convertToChatEntity(ChatDTO chatDTO) {
        Chat chat = new Chat();
        chat.setType(chatDTO.getType());
        chat.setMessage(chatDTO.getMessage());
        chat.setRoomId(chatDTO.getRoomId());
        chat.setSender(chatDTO.getSender());
        chat.setTimestamp(LocalDateTime.now()); // 메시지 수신 시간을 현재 시간으로 설정
        return chat;
    }


    public ResponseEntity<?> createRoom(String name) {
        List<ChatRoom> rooms = chatRoomRepository.findByName(name);
        if (!rooms.isEmpty()) {
            return ResponseEntity.badRequest().body("Room with the same name already exists");
        }

        ChatRoom newRoom = new ChatRoom();
        newRoom.setRoomId(UUID.randomUUID().toString());
        newRoom.setName(name);
        chatRoomRepository.save(newRoom);

        return ResponseEntity.ok(convertToRoomDTO(newRoom));
    }

    public ResponseEntity<?> joinRoomByName(String roomName, String userId) {
        List<ChatRoom> chatRooms = chatRoomRepository.findByName(roomName);
        if (chatRooms.isEmpty()) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body("Room not found");
        }

        ChatRoom chatRoom = chatRooms.get(0); // roomName이 유니크하다고 가정
        if (!chatRoom.getUserIds().contains(userId)) {
            chatRoom.getUserIds().add(userId);
            chatRoomRepository.save(chatRoom);
        }

        // 사용자가 이미 채팅방에 있는 경우, 여기서 추가적인 처리 없이 OK 응답 반환
        return ResponseEntity.ok(convertToRoomDTO(chatRoom));
    }


    public ChatDTO processMessage(ChatDTO chatDTO) {
        Optional<ChatRoom> chatRoom = chatRoomRepository.findById(chatDTO.getRoomId());
        if (chatRoom.isPresent()) {
            // 엔티티 변환 과정에서 timestamp와 id 생성
            Chat chat = convertToChatEntity(chatDTO);
            // db저장 후 다시 DTO로 변환해서 컨트롤러로 전달 => broadcast
            return convertToChatDTO(chatRepository.save(chat));
        } else {
            log.error("Chat room not found: {}", chatDTO.getRoomId());
        }
        return chatDTO;
    }

    public List<ChatRoomDTO> getRoomsByUserId(String userId) {
        List<ChatRoom> chatRooms = chatRoomRepository.findAll(); // 나중에 수정해보기
        List<ChatRoom> filteredRooms = chatRooms.stream()
                .filter(room -> room.getUserIds().contains(userId))
                .toList();

        return filteredRooms.stream()
                .map(this::convertToRoomDTO)
                .collect(Collectors.toList());
    }

    public List<ChatDTO> getChatHistory(String roomId) {
        List<Chat> chats = chatRepository.findByRoomId(roomId);
        return chats.stream()
                .map(this::convertToChatDTO)
                .collect(Collectors.toList());
    }

    public List<ChatDTO> searchChatMessages(String roomId, String keyword) {
        List<Chat> chats = chatRepository.findByRoomId(roomId);
        return chats.stream()
                .filter(chat -> chat.getMessage().contains(keyword))
                .map(this::convertToChatDTO)
                .collect(Collectors.toList());
    }

}
