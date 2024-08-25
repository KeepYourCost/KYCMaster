package com.example.kyc.member.controller;

import com.example.kyc.common.dto.Message;
import com.example.kyc.handler.CustomException;
import com.example.kyc.handler.StatusCode;
import com.example.kyc.member.dto.ReqSignInDto;
import com.example.kyc.member.dto.ReqSignUpDto;
import com.example.kyc.member.service.MemberService;
import com.example.kyc.security.dto.Token;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;

import org.springframework.http.ResponseEntity;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;

import java.io.IOException;


@RequiredArgsConstructor
@RestController
@RequestMapping("/member")
public class MemberController {
    private final PasswordEncoder passwordEncoder;
    private final MemberService memberService;

    @PostMapping("/signin")
    public ResponseEntity<Message> login(@RequestBody ReqSignInDto reqSignInDto, @RequestHeader("User-Agent") String userAgent) throws IOException {
        Token token = memberService.getToken(reqSignInDto, userAgent, passwordEncoder);
        return ResponseEntity.ok(new Message(StatusCode.OK, token));
    }

    @PostMapping(value = "/signup")
    public ResponseEntity<Message> signup(@RequestBody ReqSignUpDto reqSignUpDto) throws IOException {
        reqSignUpDto.encodePassword(passwordEncoder);
        memberService.saveMemberInfo(reqSignUpDto);
        return ResponseEntity.ok(new Message(StatusCode.OK));
    }
}
