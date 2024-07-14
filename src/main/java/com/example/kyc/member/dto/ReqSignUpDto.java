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
    @Email(message = "Invalid email format")
    private String email;
    private String password;
    private String role;
    private String name;
    @Pattern(regexp = "^01[0-9]{1}-[0-9]{4}-[0-9]{4}$")
    private String phoneNum;

    @Builder
    public ReqSignUpDto(String email, String password, String role, String name, String phoneNum) {
        this.email = email;
        this.password = password;
        this.role = role;
        this.name = name;
        this.phoneNum = phoneNum;
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
                .phoneNum(this.phoneNum)
                .build();
    }
}
