package com.knusdp.SmartLedger.controller;

import com.knusdp.SmartLedger.dto.LoginRequestDto;
import com.knusdp.SmartLedger.dto.SaveUserLoginInfoDto;
import com.knusdp.SmartLedger.entity.User;
import com.knusdp.SmartLedger.service.AuthService;
import com.knusdp.SmartLedger.service.UserService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.Map;
@RequiredArgsConstructor
@RestController
@RequestMapping("/users")
public class AuthController {

    private final AuthService authService;
    private final UserService userService;

    @PostMapping("/login")
    public ResponseEntity<?> login(@RequestBody Map<String, String> loginData){
        String email = loginData.get("email");
        String password = loginData.get("password");


        LoginRequestDto response = authService.login(email, password);

        if(response != null){
            return ResponseEntity.ok(response); // 로그인 성공: 사용자 정보 반환
        } else {
            return ResponseEntity.status(401).body("로그인 실패");
        }
    }

    @PostMapping
    public ResponseEntity<?> signUp(@RequestBody SaveUserLoginInfoDto dto) {
        try {
            User saved = userService.saveUserInfo(dto);
            return ResponseEntity.ok(saved); // 성공 시 저장된 유저 반환
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        }
    }
}
