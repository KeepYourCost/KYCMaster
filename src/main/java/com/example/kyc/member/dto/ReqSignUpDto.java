package com.example.kyc.member.dto;

import com.example.kyc.member.entity.Member;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.Pattern;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.springframework.security.crypto.password.PasswordEncoder;

@Getter
@NoArgsConstructor
public class ReqSignUpDto {
    private String email;
    private String password;
    private String role;
    private String name;

    @Builder
    public ReqSignUpDto(String email, String password, String role, String name) {
        this.email = email;
        this.password = password;
        this.role = role;
        this.name = name;
    }
    public void encodePassword(PasswordEncoder passwordEncoder) {
        this.password = passwordEncoder.encode(this.password);
    }

    public Member toEntity() {
        return Member.builder()
                .email(this.email)
                .password(this.password)
                .role(this.role)
                .name(this.name)
                .build();
    }
}
