package com.knusdp.SmartLedger.controller;

import com.knusdp.SmartLedger.dto.goal.CreateGoalRequestDto;
import com.knusdp.SmartLedger.dto.goal.GoalResponseDto;
import com.knusdp.SmartLedger.dto.UpdateGoalRequestDto;
import com.knusdp.SmartLedger.service.GoalService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import jakarta.validation.Valid;
import jakarta.validation.Validator;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/goals")
@RequiredArgsConstructor
@SecurityRequirement(name = "bearerAuth")
public class GoalController {

    private final GoalService goalService;
    private final Validator validator;

    @Operation(summary = "목표 생성 (이미지 업로드 포함)")
    @PostMapping(consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<Map<String, Object>> createGoal(
            @ModelAttribute @Valid CreateGoalRequestDto createGoalRequestDto
    ) {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        Long userId = Long.parseLong(authentication.getName());

        Long goalId = goalService.createGoal(userId, createGoalRequestDto);

        return ResponseEntity.status(HttpStatus.CREATED)
                .body(Map.of(
                        "goalId", goalId,
                        "message", "목표가 생성되었습니다."
                ));
    }


    @GetMapping
    public ResponseEntity<List<GoalResponseDto>> getGoals() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        Long userId = Long.parseLong(authentication.getName());
        List<GoalResponseDto> response = goalService.findGoalsByMemberId(userId);
        return ResponseEntity.ok(response);
    }

    @GetMapping("/{id}")
    public ResponseEntity<GoalResponseDto> getGoal(@PathVariable("id") Long goalId) {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        Long userId = Long.parseLong(authentication.getName());
        GoalResponseDto response = goalService.findGoalById(userId, goalId);
        return ResponseEntity.ok(response);
    }

    @PatchMapping("/{id}")
    public ResponseEntity<GoalResponseDto> updateGoal(@PathVariable("id") Long goalId, @RequestBody UpdateGoalRequestDto dto) {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        Long userId = Long.parseLong(authentication.getName());
        GoalResponseDto updatedGoal = goalService.updateGoal(userId, goalId, dto);
        return ResponseEntity.ok(updatedGoal);
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Map<String, String>> deleteGoal(@PathVariable("id") Long goalId) {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        Long userId = Long.parseLong(authentication.getName());
        goalService.deleteGoal(userId, goalId);
        return ResponseEntity.ok(Map.of("message", "목표가 삭제되었습니다."));
    }
}