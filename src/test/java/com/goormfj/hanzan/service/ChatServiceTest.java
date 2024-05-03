package com.goormfj.hanzan.service;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.*;

import com.goormfj.hanzan.chat.domain.ChatRoom;
import com.goormfj.hanzan.chat.domain.ChatRoomDTO;
import com.goormfj.hanzan.chat.repository.ChatRoomRepository;
import com.goormfj.hanzan.chat.service.ChatService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

import java.util.Objects;


public class ChatServiceTest {

    @Mock
    private ChatRoomRepository chatRoomRepository;

    @InjectMocks
    private ChatService chatService;

    private AutoCloseable closeable;

    @BeforeEach
    void setup() {
        closeable = MockitoAnnotations.openMocks(this);
    }

    @AfterEach
    void releaseMocks() throws Exception {
        closeable.close();
    }

//    @Test
//    void whenCreateRoom_thenRoomIsCreated() {
//        String roomName = "Test Room";
//        ChatRoom chatRoom = new ChatRoom();
//        chatRoom.setName(roomName);
//        chatRoom.setRoomId("123");
//
//        when(chatRoomRepository.save(any(ChatRoom.class))).thenReturn(chatRoom);
//
//        ResponseEntity<?> response = chatService.createRoom(roomName);
//        assertEquals(HttpStatus.OK, response.getStatusCode());
//        assertEquals(roomName, ((ChatRoomDTO) Objects.requireNonNull(response.getBody())).getName());
//    }
}
