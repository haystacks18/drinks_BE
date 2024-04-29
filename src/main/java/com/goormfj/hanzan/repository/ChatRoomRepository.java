package com.goormfj.hanzan.repository;

import com.goormfj.hanzan.domain.ChatRoom;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface ChatRoomRepository extends JpaRepository<ChatRoom, String> {
    List<ChatRoom> findByName(String name);
}
