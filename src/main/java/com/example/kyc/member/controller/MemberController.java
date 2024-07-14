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


@RequiredArgsConstructor
@RestController
@RequestMapping("/member")
public class MemberController {
    private final PasswordEncoder passwordEncoder;
    private final MemberService memberService;

    @PostMapping("/signin")
    public ResponseEntity<Message> login(@Valid @RequestBody ReqSignInDto reqSignInDto, BindingResult bindingResult, @RequestHeader("User-Agent") String userAgent) {
        if(bindingResult.hasErrors()) throw new CustomException(StatusCode.INVALID_DATA_FORMAT);
        Token token = memberService.getToken(reqSignInDto, userAgent, passwordEncoder);
        return ResponseEntity.ok(new Message(StatusCode.OK, token));
    }

    @PostMapping(value = "/signup")
    public ResponseEntity<Message> signup(@Valid @RequestBody ReqSignUpDto reqSignUpDto, BindingResult bindingResult) {
        if(bindingResult.hasErrors()) throw new CustomException(StatusCode.INVALID_DATA_FORMAT);
        reqSignUpDto.encodePassword(passwordEncoder);
        memberService.saveMemberInfo(reqSignUpDto);
        return ResponseEntity.ok(new Message(StatusCode.OK));
    }
}
