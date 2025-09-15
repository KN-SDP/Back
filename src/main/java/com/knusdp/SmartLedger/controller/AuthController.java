package com.knusdp.SmartLedger.controller;

import com.knusdp.SmartLedger.dto.LoginRequestDto;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;

import java.util.Map;

@Controller
public class AuthController {
    @PostMapping("/users/sign-up")
    public ResponseEntity<?> login(@RequestBody Map<String, String> loginData){
        String userID = loginData.get("userID");
        String password = loginData.get("password");

        LoginRequestDto response =

        if(response != null){
            return ResponseEntity.ok(response); // 로그인 성공: 사용자 정보 반환
        } else {
            return ResponseEntity.status(401).body("로그인 실패");
        }
    }
}
