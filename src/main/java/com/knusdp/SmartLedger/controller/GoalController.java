package com.knusdp.SmartLedger.controller;

import com.knusdp.SmartLedger.dto.CreateGoalRequestDto;
import com.knusdp.SmartLedger.dto.GoalResponseDto;
import com.knusdp.SmartLedger.dto.UpdateGoalRequestDto;
import com.knusdp.SmartLedger.entity.Goal;
import com.knusdp.SmartLedger.entity.Member;
import com.knusdp.SmartLedger.exception.UserNotFoundException;
import com.knusdp.SmartLedger.repository.GoalRepository;
import com.knusdp.SmartLedger.repository.MemberRepository;
import com.knusdp.SmartLedger.service.GoalService;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import jakarta.validation.Valid; // @Valid 사용을 위해 import
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/goals") // 목표 관련 API의 기본 경로
@RequiredArgsConstructor
@SecurityRequirement(name = "bearerAuth")

public class GoalController {

    private final GoalService goalService;
    private final GoalRepository goalRepository;
    private final MemberRepository memberRepository;

    @PostMapping
    public ResponseEntity<String> createGoal(@Valid @RequestBody CreateGoalRequestDto dto) {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        Long userId = Long.parseLong(authentication.getName());

        goalService.createGoal(userId, dto); // 서비스 호출

        // 성공 메시지 문자열과 201 Created 상태 반환
        return ResponseEntity.status(HttpStatus.CREATED)
                .body("목표가 생성되었습니다");
    }
    @GetMapping
    public ResponseEntity<List<GoalResponseDto>> getGoals() {
        // 토큰에서 현재 사용자 ID 추출
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        Long userId = Long.parseLong(authentication.getName());

        // 서비스 호출
        List<GoalResponseDto> response = goalService.findGoalsByMemberId(userId);

        // 조회 결과를 200 OK 상태와 함께 반환
        return ResponseEntity.ok(response);
    }
    @GetMapping("/{id}")
    public ResponseEntity<GoalResponseDto> getGoal(@PathVariable("id") Long goalId) {
        // 토큰에서 현재 사용자 ID 추출
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        Long userId = Long.parseLong(authentication.getName());

        // 서비스 호출
        GoalResponseDto response = goalService.findGoalById(userId, goalId);

        return ResponseEntity.ok(response);
    }
    @PatchMapping("/{id}")
    public ResponseEntity<GoalResponseDto> updateGoal(
            @PathVariable("id") Long goalId,
            @Valid @RequestBody UpdateGoalRequestDto dto // @Valid 추가 (선택적)
    ) {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        Long userId = Long.parseLong(authentication.getName());

        GoalResponseDto updatedGoal = goalService.updateGoal(userId, goalId, dto);

        return ResponseEntity.ok(updatedGoal); // 200 OK와 함께 수정된 목표 정보 반환
    }
    @DeleteMapping("/{id}")
    public ResponseEntity<Map<String, String>> deleteGoal(@PathVariable("id") Long goalId) { // 반환 타입을 Map으로 변경
        // 토큰에서 현재 사용자 ID 추출
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        Long userId = Long.parseLong(authentication.getName());

        // 서비스 호출하여 목표 삭제
        goalService.deleteGoal(userId, goalId);

        // Map을 사용하여 성공 메시지 반환
        return ResponseEntity.ok(Map.of("message", "목표가 삭제되었습니다."));
    }
}