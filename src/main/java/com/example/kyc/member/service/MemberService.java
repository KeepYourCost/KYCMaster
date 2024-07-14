package com.example.kyc.member.service;

import com.example.kyc.member.dto.ReqSignInDto;
import com.example.kyc.member.dto.ReqSignUpDto;
import com.example.kyc.security.dto.Token;
import org.springframework.security.crypto.password.PasswordEncoder;


public interface MemberService {
    Token getToken(ReqSignInDto reqSignInDto, String userAgent, PasswordEncoder passwordEncoder);
    void saveMemberInfo(ReqSignUpDto reqSignUpDto);
}
