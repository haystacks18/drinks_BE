package com.goormfj.hanzan.chat.domain;

import lombok.Builder;
import lombok.Data;
import lombok.Getter;
import lombok.Setter;
import org.springframework.web.socket.WebSocketSession;

import java.time.LocalDateTime;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

@Data
@Getter
@Setter
public class ChatRoomDTO {
    private String roomId;
    private String name;
    private List<String> userIds;
    private String thumbnailUrl;

    private String lastMessage;
    private LocalDateTime lastMessageTime;
    private int membersCount;

    private Set<WebSocketSession> sessions = new HashSet<>();

    @Builder
    public ChatRoomDTO(String roomId, String name){
        this.roomId = roomId;
        this.name = name;
    }

}
