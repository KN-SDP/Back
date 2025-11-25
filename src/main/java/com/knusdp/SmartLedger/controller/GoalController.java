package com.knusdp.SmartLedger.controller;

import com.knusdp.SmartLedger.dto.CreateGoalRequestDto;
import com.knusdp.SmartLedger.dto.GoalResponseDto;
import com.knusdp.SmartLedger.dto.UpdateGoalRequestDto;
import com.knusdp.SmartLedger.service.GoalService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/goals")
@RequiredArgsConstructor
@SecurityRequirement(name = "bearerAuth")
public class GoalController {

    private final GoalService goalService;

    @PostMapping(consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<String> createGoal(
            @ModelAttribute @Valid CreateGoalRequestDto dto,               // 텍스트 필드 자동 매핑 + 자동 validation
            @RequestPart(value = "image", required = false) MultipartFile image  // 파일 따로 받기
    ) {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        Long userId = Long.parseLong(authentication.getName());

        goalService.createGoal(userId, dto, image);

        return ResponseEntity.status(HttpStatus.CREATED).body("목표가 생성되었습니다");
    }

    // ... (나머지 조회, 수정, 삭제 메서드는 기존과 동일하게 유지) ...
    @GetMapping
    public ResponseEntity<List<GoalResponseDto>> getGoals() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        Long userId = Long.parseLong(authentication.getName());
        return ResponseEntity.ok(goalService.findGoalsByMemberId(userId));
    }

    @GetMapping("/{id}")
    public ResponseEntity<GoalResponseDto> getGoal(@PathVariable("id") Long goalId) {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        Long userId = Long.parseLong(authentication.getName());
        return ResponseEntity.ok(goalService.findGoalById(userId, goalId));
    }

    @PatchMapping("/{id}")
    public ResponseEntity<GoalResponseDto> updateGoal(@PathVariable("id") Long goalId, @RequestBody UpdateGoalRequestDto dto) {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        Long userId = Long.parseLong(authentication.getName());
        return ResponseEntity.ok(goalService.updateGoal(userId, goalId, dto));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Map<String, String>> deleteGoal(@PathVariable("id") Long goalId) {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        Long userId = Long.parseLong(authentication.getName());
        goalService.deleteGoal(userId, goalId);
        return ResponseEntity.ok(Map.of("message", "목표가 삭제되었습니다."));
    }
}