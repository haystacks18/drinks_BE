package com.goormfj.hanzan.user.domain;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.LocalDate;

@Entity
@Getter
@NoArgsConstructor
public class Member {

    @Id
    @GeneratedValue(strategy = GenerationType.SEQUENCE)
    private Long id;

    @Column(nullable = false)
    private String userId; // 회원 입력 아이디

    @Column(nullable = false)
    private String password; // 해시 처리된 비밀번호

    @Column(nullable = false)
    private String email;

    private String name;

    private LocalDate localDate;
    private String phone_Number;

//    @Enumerated(value = EnumType.STRING)
//    private Gender gender;
    private String introduction;
    private String profile_picture;

//    @Embedded
//    private Preferences preferences; // 취향

    public Member(String userId, String password, String email) {
        this.userId = userId;
        this.password = password;
        this.email = email;
    }
}