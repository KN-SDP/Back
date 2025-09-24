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
    public ResponseEntity<LoginResponseDto> login(@RequestBody LoginRequestDto loginRequestDto) {
        // 로그인 실패 시 서비스에서 예외를 던지므로, 컨트롤러는 성공 로직만 처리합니다.
        LoginResponseDto response = authService.login(loginRequestDto.getEmail(), loginRequestDto.getPassword());
        return ResponseEntity.ok(response);
    }

    @PostMapping("/sign-up")
    public ResponseEntity<UserResponseDto> signUp(@RequestBody SaveUserLoginInfoDto dto) {
        // try-catch가 사라졌습니다. 모든 예외는 GlobalExceptionHandler가 처리합니다.
        Member savedMember = userService.saveUserInfo(dto);

        // 엔티티 대신 필요한 정보만 담은 DTO로 변환하여 반환합니다.
        UserResponseDto responseDto = new UserResponseDto(
                savedMember.getId(),
                savedMember.getEmail(),
                savedMember.getUsername(),
                savedMember.getNickname()
        );

        // 리소스 생성 성공을 의미하는 201 Created 상태 코드를 반환합니다.
        return ResponseEntity.status(HttpStatus.CREATED).body(responseDto);
    }

    @PostMapping("/recover-id")
    public ResponseEntity<FindIdResponseDto> findId(@RequestBody FindIdRequestDto request) {
        // try-catch와 if-else가 모두 사라졌습니다.
        // 서비스에서 사용자를 찾지 못하면 UserNotFoundException을 던집니다.
        String foundEmail = authService.findId(
                request.getName(),
                request.getPhoneNum(),
                request.getBirth()
        );

        FindIdResponseDto responseDto = new FindIdResponseDto(
                HttpStatus.OK.value(),
                "가입된 이메일을 확인했습니다.",
                foundEmail,
                request.getBirth()
        );

        return ResponseEntity.ok(responseDto);
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