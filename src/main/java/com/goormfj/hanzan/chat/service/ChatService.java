package com.goormfj.hanzan.chat.service;

import com.goormfj.hanzan.chat.domain.*;
import com.goormfj.hanzan.chat.repository.ChatRepository;
import com.goormfj.hanzan.chat.repository.ChatRoomRepository;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.Data;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Service;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.server.ResponseStatusException;

import java.time.LocalDateTime;
import java.util.*;
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

    @Autowired
    private SimpMessagingTemplate messagingTemplate;

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


    public ResponseEntity<?> createRoom(String name, String thumbnailUrl, List<String> initialMembers) {
        // 요청 유효성 검사
        if (name == null || name.trim().isEmpty()) {
            return ResponseEntity.badRequest().body("Room name cannot be empty");
        }

        if (name.length() > 25) {
            return ResponseEntity.badRequest().body("Room name is too long");
        }

//        List<ChatRoom> rooms = chatRoomRepository.findByName(name);
//        if (!rooms.isEmpty()) {
//            return ResponseEntity.badRequest().body("Room with the same name already exists");
//        }

        ChatRoom newRoom = new ChatRoom();
        newRoom.setRoomId(UUID.randomUUID().toString());
        newRoom.setThumbnailUrl(thumbnailUrl);
        newRoom.setName(name);
        newRoom.setUserIds(new HashSet<>(initialMembers));  // 초기 멤버 설정
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

    public ResponseEntity<?> joinRoom(String roomId, String userId) {
        // findById로 ChatRoom 찾기
        Optional<ChatRoom> chatRoomOptional = chatRoomRepository.findById(roomId);

        // Optional의 isPresent() 메서드로 채팅방 존재 여부 확인
        if (chatRoomOptional.isEmpty()) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body("Room not found");
        }

        // get()으로 Optional에서 ChatRoom 인스턴스 가져오기
        ChatRoom chatRoom = chatRoomOptional.get();

        // 사용자가 이미 채팅방에 있는지 확인하고 없다면 추가
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
        List<ChatRoom> chatRooms = chatRoomRepository.findAllByUserId(userId);
        return chatRooms.stream().map(room -> {
            ChatRoomDTO roomDTO = convertToRoomDTO(room);
            Chat latestChat = chatRepository.findTopByRoomIdOrderByTimestampDesc(room.getRoomId());
            if (latestChat != null) {
                roomDTO.setLastMessage(latestChat.getMessage());
                roomDTO.setLastMessageTime(latestChat.getTimestamp());
            }
            roomDTO.setMembersCount(room.getUserIds().size());
            return roomDTO;
        }).collect(Collectors.toList());
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

    public ResponseEntity<?> addMembersToRoom(String roomId, Set<String> userIds) {
        ChatRoom room = chatRoomRepository.findById(roomId).orElse(null);
        if (room == null) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body("Room not found");
        }

        boolean changed = false;
        for (String userId : userIds) {
            if (room.getUserIds().add(userId)) {
                sendNotification(roomId, userId + " has joined the room.", ChatDTO.MessageType.ENTER);
                changed = true;
            }
        }

        if (changed) {
            chatRoomRepository.save(room);
        }

        return ResponseEntity.ok("Members added to the room successfully");
    }

    public ResponseEntity<?> removeMembersFromRoom(String roomId, Set<String> userIds) {
        ChatRoom room = chatRoomRepository.findById(roomId).orElse(null);
        if (room == null) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body("Room not found");
        }

        boolean changed = false;
        for (String userId : userIds) {
            if (room.getUserIds().remove(userId)) {
                sendNotification(roomId, userId + " has left the room.", ChatDTO.MessageType.LEAVE);
                changed = true;
            }
        }

        if (changed) {
            chatRoomRepository.save(room);
        }

        return ResponseEntity.ok("Members removed from the room successfully");
    }

    public void sendNotification(String roomId, String message, ChatDTO.MessageType type) {
        ChatDTO notification = ChatDTO.builder()
                .type(type)
                .message(message)
                .roomId(roomId)
                .sender("System")
                .timestamp(LocalDateTime.now())
                .build();

        // 채팅방의 모든 세션에 메시지 전송
        messagingTemplate.convertAndSend(String.format("/topic/%s", roomId), notification);
    }

    public List<String> getRoomMembers(String roomId) {
        Optional<ChatRoom> roomOptional = chatRoomRepository.findById(roomId);
        if (roomOptional.isEmpty()) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, "Room not found");
        }
        ChatRoom room = roomOptional.get();
        return new ArrayList<>(room.getUserIds());  // Set을 List로 변환하여 반환
    }

}
