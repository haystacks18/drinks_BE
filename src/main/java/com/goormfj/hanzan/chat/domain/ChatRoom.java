package com.goormfj.hanzan.chat.domain;

import jakarta.persistence.ElementCollection;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.Id;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

@Entity
@Getter
@Setter
@NoArgsConstructor
public class ChatRoom {
    @Id
    private String roomId;

    private String name;

    private String thumbnailUrl;

    @ElementCollection(fetch = FetchType.EAGER)
    private Set<String> userIds = new HashSet<>();

}
