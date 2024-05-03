package com.goormfj.hanzan.chat.repository;

import com.goormfj.hanzan.chat.domain.ChatRoom;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;

public interface ChatRoomRepository extends JpaRepository<ChatRoom, String> {
    List<ChatRoom> findByName(String name);
    @Query("SELECT cr FROM ChatRoom cr WHERE :userId MEMBER OF cr.userIds")
    List<ChatRoom> findAllByUserId(@Param("userId") String userId);
}
