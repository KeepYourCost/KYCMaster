package com.example.kyc.security.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor
public class AuthorizerDto {
    private Long memberId;
    private String name;


    @Builder
    public AuthorizerDto(Long memberId, String name) {
        this.memberId = memberId;
        this.name = name;
    }

    @Getter
    @AllArgsConstructor
    public enum ClaimName {
        ID("ID"),
        NAME("NAME");
        private final String value;
    }
}
