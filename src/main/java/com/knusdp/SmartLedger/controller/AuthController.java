package com.knusdp.SmartLedger.controller;

import com.knusdp.SmartLedger.dto.LoginResponseDto;
import com.knusdp.SmartLedger.dto.SaveUserLoginInfoDto;
import com.knusdp.SmartLedger.entity.User;
import com.knusdp.SmartLedger.service.AuthService;
import com.knusdp.SmartLedger.service.UserService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.*;

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


        LoginResponseDto response = authService.login(email, password);

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
//앞으로 리팩터링 포인트
//
//Map<String, String> 대신 LoginRequestDto 사용
//
//User 직접 반환 ❌ → UserResponseDto 반환 ✅
//
//상태 코드 정리 (회원가입 → 201 Created)
//
//전역 예외 처리(@RestControllerAdvice)로 예외 핸들링 분리
//
//JWT 붙이면 LoginResponseDto 추가