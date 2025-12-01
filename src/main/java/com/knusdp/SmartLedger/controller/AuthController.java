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

        return ResponseEntity.ok(response);
    }

    /* 회원가입 */
    @PostMapping("/sign-up")
    public ResponseEntity<?> signUp(@RequestBody SaveUserLoginInfoDto dto) {
        memberService.saveUserInfo(dto);
        return ResponseEntity.ok("가입이 완료되었습니다.");
    }

    /* 아이디 찾기 */
    @PostMapping("/recover-id")
    public ResponseEntity<?> findId(@RequestBody FindIdRequestDto request) {
        String foundEmail = findInFoService.findId(
                request.getName(),
                request.getPhoneNum(),
                request.getBirth()
        );
        return ResponseEntity.ok(
                Map.of(
                        "message", "가입된 이메일을 확인했습니다.",
                        "email", foundEmail
                )
        );
    }


    //로그인 중 비번 변경
    @PatchMapping("/password")
    @SecurityRequirement(name = "bearerAuth")
    public ResponseEntity<?> changePassword(
            @Valid @RequestBody ChangePasswordDto dto,
            Authentication authentication
    ) {
        Long userId = Long.parseLong(authentication.getName());

        memberService.changePassword(userId, dto.getCurrentPassword(), dto.getNewPassword(), dto.getCheckedPassword());

        return ResponseEntity.ok(Map.of("message", "비밀번호가 성공적으로 변경되었습니다."));
    }

    //로그인 안 했을 때 비번 변경
    @PostMapping("/recover-password")
    public ResponseEntity<?> issueResetToken(@RequestBody RecoverPasswordRequestDto dto) {

        String resetToken = memberService.issueResetToken(
                dto.getEmail(),
                dto.getName(),
                dto.getBirth(),
                dto.getPhone()
        );

        return ResponseEntity.ok(Map.of(
                "message", "사용자 정보가 확인되었습니다. 비밀번호를 재설정해주세요.",
                "resetToken", resetToken
        ));
    }

    @PostMapping("/recover-password/reset")
    public ResponseEntity<?> resetPassword(@RequestBody ResetPasswordDto dto) {

        memberService.resetPasswordByToken(
                dto.getResetToken(),
                dto.getNewPassword(),
                dto.getCheckedPassword()
        );

        return ResponseEntity.ok(Map.of(
                "message", "비밀번호가 성공적으로 변경되었습니다."
        ));
    }


    //닉네임 변경
    @PatchMapping("/nickname")
    @SecurityRequirement(name = "bearerAuth")
    @Transactional
    public ResponseEntity<NicknameResponseDto> updateNickname(@Valid @RequestBody ChangeNicknameDto request) {

        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        Long userId = Long.parseLong(authentication.getName());

        String updatedNickname = memberService.updateNickname(userId, request.getChange_nickname());

        return ResponseEntity.ok(new NicknameResponseDto(updatedNickname));
    }


    //이메일 중복 확인
    @PostMapping("/check-email")
    public ResponseEntity<Boolean> checkEmail(@RequestBody EmailCheckRequest request) {
        return ResponseEntity.ok(memberService.isEmailAvailable(request.email()));
    }


    //회원탈퇴 소프트삭제
    @PatchMapping("/withdraw")
    @SecurityRequirement(name = "bearerAuth")
    @Transactional
    public ResponseEntity<Map<String, String>> withdraw(@RequestBody WithdrawRequest request) {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        Long userId = Long.parseLong(authentication.getName());

        authService.withdraw(userId, request.currentPassword());
    return ResponseEntity.ok(Map.of("message", "회원 탈퇴가 완료되었습니다. 14일 이내 복구가 가능합니다."));
    }

    //계정 복구
    @PatchMapping("/restore")
    public ResponseEntity<Map<String, String>> restoreAccount(@RequestBody RestoreRequest request) {
        authService.restoreAccount(request.email());
        return ResponseEntity.ok(Map.of(
                "message", "계정 복구가 완료되었습니다. 다시 로그인해주세요."
        ));
    }

}
