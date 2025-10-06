package com.knusdp.SmartLedger.controller;

import com.knusdp.SmartLedger.dto.*;
import com.knusdp.SmartLedger.service.AuthService;
import com.knusdp.SmartLedger.service.FindInFoService;
import com.knusdp.SmartLedger.service.MemberService;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;

import java.util.Map;
import java.util.Optional;

@RequiredArgsConstructor
@RestController
@RequestMapping("/users")
public class AuthController {

    private final AuthService authService;
    private final MemberService memberService;
    private final FindInFoService findInFoService;
    /*로그인*/
    @PostMapping("/login")
    public ResponseEntity<?> login(@RequestBody LoginRequestDto loginRequestDto){

        LoginResponseDto response = authService.login(loginRequestDto.getEmail(), loginRequestDto.getPassword());

        if(response != null){
            return ResponseEntity.ok(response);
        } else {
            return ResponseEntity.status(401).body("로그인실패");
        }
    }
    /*회원가입*/
    @PostMapping("/sign-up")
    public ResponseEntity<?> signUp(@RequestBody SaveUserLoginInfoDto dto) {
        try {
            memberService.saveUserInfo(dto);
            return ResponseEntity.ok("가입이 완료되었습니다.");
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        }
    }
    /*아이디 찾기*/
    @PostMapping("/recover-id")
    public ResponseEntity<FindIdResponseDto> findId(@RequestBody FindIdRequestDto request) {
        // 성공 시 200 OK, 실패 시 서비스가 예외를 던지면 GlobalExceptionHandler가 404 등을 처리
        String foundEmail = findInFoService.findId(
                request.getName(),
                request.getPhoneNum(),
                request.getBirth()
        );

        FindIdResponseDto responseDto = new FindIdResponseDto(
                HttpStatus.OK.value(),
                "가입된 이메일을 확인했습니다.",
                foundEmail
        );
        return ResponseEntity.ok(responseDto);
    }
    /*비밀번호 찾기*/
    @PostMapping("/recover-password")
    public ResponseEntity<?> recoverPassword(@RequestBody RecoverPasswordRequestDto dto) {
        boolean valid = memberService.validateMember(dto.getEmail(), dto.getName(), dto.getBirth(), dto.getPhone());

        if (valid) {
            return ResponseEntity.ok(Map.of("message", "사용자 정보가 확인되었습니다. 비밀번호를 재설정해주세요."));
        } else {
            return ResponseEntity.status(404).body(Map.of(
                    "error_code", "UserNotFound",
                    "message", "입력한 정보와 일치하는 사용자가 없습니다."
            ));
        }
    }
    /*비밀번호 재설정*/
    @PostMapping("/recover-password/reset")
    public ResponseEntity<?> resetPassword(@RequestBody ResetPasswordDto dto) {
        try {
            boolean success = memberService.resetPassword(dto.getEmail(), dto.getNewPassword(), dto.getCheckedPassword());

            if (success) {
                return ResponseEntity.ok(Map.of("message", "비밀번호가 성공적으로 변경되었습니다."));
            } else {
                return ResponseEntity.status(404).body(Map.of(
                        "error_code", "UserNotFound",
                        "message", "등록되지 않은 사용자입니다."
                ));
            }
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().body(Map.of(
                    "error_code", "PasswordMismatch",
                    "message", "비밀번호 확인이 일치하지 않습니다."
            ));
        }
    }
    @Transactional
    @PatchMapping("/nickname")
    public ResponseEntity<String> updateNickname(@RequestBody UserInFoDto dto) {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        Long userId = Long.parseLong(authentication.getName());

        memberService.updateNickname(userId, dto.getUserNickName());

        return ResponseEntity.ok("닉네임이 성공적으로 변경되었습니다.");
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