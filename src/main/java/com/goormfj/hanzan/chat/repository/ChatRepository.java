package com.goormfj.hanzan.chat.repository;

import com.goormfj.hanzan.chat.domain.Chat;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface ChatRepository extends JpaRepository<Chat, Long> {
    List<Chat> findByRoomId(String roomId);
    Chat findTopByRoomIdOrderByTimestampDesc(String roomId);
}
