package com.example.kyc.security;



import com.example.kyc.member.entity.Member;
import com.example.kyc.security.dto.AuthorizerDto;
import com.example.kyc.security.dto.Token;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.SignatureAlgorithm;
import jakarta.annotation.PostConstruct;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;


import java.util.Base64;
import java.util.Date;

@RequiredArgsConstructor
@Component
public class JwtCreator {


    @Value("${jwt.access.key}")
    private String accessSecretKey;
    @Value("${jwt.access.validTime}")
    private long accessTokenValidTime;


    @PostConstruct
    protected void init() {
        accessSecretKey = Base64.getEncoder().encodeToString(accessSecretKey.getBytes());
    }

    // 토큰 생성
    public Token createToken(Member member) {  // userPK = email
        Claims claims = Jwts.claims().setSubject(member.getEmail()); // JWT payload 에 저장되는 정보단위
        Date now = new Date();
        String accessToken = getToken(member, claims, now, accessTokenValidTime, accessSecretKey);
        return Token.builder()
                .accessToken(accessToken)
                .key(member.getEmail()).build();
    }

    private String getToken(Member member, Claims claims, Date currentTime, long tokenValidTime, String secretKey) {
        return Jwts.builder()
                .setClaims(claims) //정보 저장
                .claim(AuthorizerDto.ClaimName.ID.getValue(), String.valueOf(member.getMemberId()))
                .claim(AuthorizerDto.ClaimName.NAME.getValue(), member.getName())
                .setIssuedAt(currentTime)  //토큰 발행시간 정보
                .setExpiration(new Date(currentTime.getTime() + tokenValidTime)) //Expire Time
                .signWith(SignatureAlgorithm.HS256, secretKey)  //암호화 알고리즘
                .compact();

    }


}