package com.knusdp.SmartLedger.controller;

import com.knusdp.SmartLedger.dto.*;
import com.knusdp.SmartLedger.service.AuthService;
import com.knusdp.SmartLedger.service.FindInFoService;
import com.knusdp.SmartLedger.service.MemberService;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import jakarta.transaction.Transactional;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;
import java.util.Map;

@RequiredArgsConstructor
@RestController
@RequestMapping("/users")
public class AuthController {

    private final AuthService authService;
    private final MemberService memberService;
    private final FindInFoService findInFoService;

    /* 로그인 */
    @PostMapping("/login")
    public ResponseEntity<?> login(@RequestBody LoginRequestDto loginRequestDto) {
        LoginResponseDto response = authService.login(loginRequestDto.getEmail(), loginRequestDto.getPassword());
        if (response != null) return ResponseEntity.ok(response);
        return ResponseEntity.status(401).body("로그인 실패");
    }

    /* 회원가입 */
    @PostMapping("/sign-up")
    public ResponseEntity<?> signUp(@RequestBody SaveUserLoginInfoDto dto) {
        try {
            memberService.saveUserInfo(dto);
            return ResponseEntity.ok("가입이 완료되었습니다.");
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        }
    }

    /* 아이디 찾기 */
    @PostMapping("/recover-id")
    public ResponseEntity<FindIdResponseDto> findId(@RequestBody FindIdRequestDto request) {
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

    @PostMapping("/recover-password")
    public ResponseEntity<?> issueResetToken(@RequestBody RecoverPasswordRequestDto dto) {
        String token = memberService.issueResetToken(dto.getEmail(), dto.getName(), dto.getBirth(), dto.getPhone());

        if (token != null) {
            return ResponseEntity.ok(Map.of(
                    "message", "사용자 정보가 확인되었습니다. 비밀번호를 재설정해주세요.",
                    "resetToken", token
            ));
        } else {
            return ResponseEntity.status(404).body(Map.of(
                    "error_code", "UserNotFound",
                    "message", "입력한 정보와 일치하는 사용자가 없습니다."
            ));
        }
    }

    @PostMapping("/recover-password/reset")
    public ResponseEntity<?> resetPassword(@RequestBody ResetPasswordDto dto) {
        try {
            boolean success = memberService.resetPasswordByToken(
                    dto.getResetToken(),
                    dto.getNewPassword(),
                    dto.getCheckedPassword()
            );

            if (success) {
                return ResponseEntity.ok(Map.of("message", "비밀번호가 성공적으로 변경되었습니다."));
            } else {
                return ResponseEntity.status(400).body(Map.of(
                        "error_code", "InvalidToken",
                        "message", "유효하지 않거나 만료된 토큰입니다."
                ));
            }
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().body(Map.of(
                    "error_code", "PasswordMismatch",
                    "message", "비밀번호 확인이 일치하지 않습니다."
            ));
        }
    }


    //닉네임 변경
    @PatchMapping("/nickname")
    @SecurityRequirement(name = "bearerAuth")
    @Transactional
    public ResponseEntity<String> updateNickname(@Valid @RequestBody ChangeNicknameDto request) {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        Long userId = Long.parseLong(authentication.getName());

        memberService.updateNickname(userId, request.getChange_nickname());

        return ResponseEntity.ok("닉네임 변경이 완료되었습니다.");
    }

    //이메일 중복 확인
    @PostMapping("/check-email")
    public ResponseEntity<Boolean> checkEmail(@RequestBody EmailCheckRequest request) {
        return ResponseEntity.ok(memberService.isEmailAvailable(request.email()));
    }


}
