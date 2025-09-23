package com.knusdp.SmartLedger.controller;

import com.knusdp.SmartLedger.dto.*;
import com.knusdp.SmartLedger.entity.Member;
import com.knusdp.SmartLedger.service.AuthService;
import com.knusdp.SmartLedger.service.UserService;
import lombok.RequiredArgsConstructor;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import java.util.Optional;

@RequiredArgsConstructor
@RestController
@RequestMapping("/users")
public class AuthController {

    private final AuthService authService;
    private final UserService userService;

    @PostMapping("/login")
    public ResponseEntity<?> login(@RequestBody LoginRequestDto loginRequestDto){

        LoginResponseDto response = authService.login(loginRequestDto.getEmail(), loginRequestDto.getPassword());

        if(response != null){
            return ResponseEntity.ok(response);
        } else {
            return ResponseEntity.status(401).body("로그인실패");
        }
    }

    @PostMapping("/sign-up")
    public ResponseEntity<?> signUp(@RequestBody SaveUserLoginInfoDto dto) {
        try {
            Member saved = userService.saveUserInfo(dto);
            return ResponseEntity.ok(saved);
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        }
    }

    @PostMapping("/recover-id")
    public ResponseEntity<?> findId(@RequestBody FindIdRequestDto request) {
        try {
            Optional<String> emailOpt = authService.findId(
                    request.getName(),
                    request.getPhoneNum(),
                    request.getBirth()
            );

            if(emailOpt.isPresent()){
                FindIdResponseDto responseDto = new FindIdResponseDto(
                        200,
                        "가입된 이메일을 확인했습니다.",
                        emailOpt.get(),
                        request.getBirth()
                );
                return ResponseEntity.ok(responseDto);
            } else {
                // 사용자 없음 → 404 처리
                ErrorResponseDto error = new ErrorResponseDto(
                        404,
                        "UserNotFound",
                        "일치하는 계정을 찾을 수 없습니다."
                );
                return ResponseEntity.status(HttpStatus.NOT_FOUND).body(error);
            }

        } catch(Exception e) {
            // 서버 오류 → 500 처리
            ErrorResponseDto error = new ErrorResponseDto(
                    500,
                    "InternalServerError",
                    "서버에 문제가 발생했습니다."
            );
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(error);
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