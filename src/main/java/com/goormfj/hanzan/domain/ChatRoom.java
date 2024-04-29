package com.goormfj.hanzan.domain;

import jakarta.persistence.ElementCollection;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.util.ArrayList;
import java.util.List;

@Entity
@Getter
@Setter
@NoArgsConstructor
public class ChatRoom {
    @Id
    private String roomId;

    private String name;

    @ElementCollection
    private List<String> userIds = new ArrayList<>();

}
