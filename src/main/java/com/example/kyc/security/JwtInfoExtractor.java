package com.example.kyc.security;


import com.example.kyc.security.dto.AuthorizerDto;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;


@Component
@RequiredArgsConstructor
public class JwtInfoExtractor {

    @Value("${jwt.access.key}")
    private String accessKey;

    public Claims extractToken(String token) {
        return Jwts.parser().setSigningKey(accessKey.getBytes()).parseClaimsJws(token).getBody();
    }

    public static AuthorizerDto getAuthorizer() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication instanceof JwtAuthenticationToken) {
            Claims claims = ((JwtAuthenticationToken) authentication).getClaims();

            return AuthorizerDto.builder()
                    .memberId(Long.parseLong(claims.get(AuthorizerDto.ClaimName.ID.getValue()).toString()))
                    .name(claims.get(AuthorizerDto.ClaimName.NAME.getValue()).toString())
                    .build();

        }
        return null;
    }
}
