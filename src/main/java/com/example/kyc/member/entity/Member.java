package com.example.kyc.member.entity;



import lombok.Builder;
import lombok.Data;




@Data
public class Member {
    private String email;
    private String password;
    private String role;
    private String name;

    @Builder
    public Member(String email, String password, String role, String name) {
        this.email = email;
        this.password = password;
        this.role = role;
        this.name = name;
    }
}
