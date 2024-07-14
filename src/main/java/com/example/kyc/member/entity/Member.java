package com.example.kyc.member.entity;


import com.example.kyc.baseTime.BaseTimeEntity;
import jakarta.persistence.*;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;


@Entity
@Getter
@NoArgsConstructor
@Table(name="member")
public class Member extends BaseTimeEntity {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "member_id", nullable = false)
    private Long memberId;

    @Column(name = "email", unique = true)
    private String email;

    @Column(name = "password")
    private String password;

    @Column(name = "role")
    private String role;

    @Column(name = "name")
    private String name;

    @Column(name = "phone_num")
    private String phoneNum;

    @Builder
    public Member(Long memberId, String email, String password, String role, String name, String phoneNum) {
        this.memberId = memberId;
        this.email = email;
        this.password = password;
        this.role = role;
        this.name = name;
        this.phoneNum = phoneNum;
    }
}
