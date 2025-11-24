package com.knusdp.SmartLedger.controller;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper; // 추가됨
import com.knusdp.SmartLedger.dto.CreateGoalRequestDto;
import com.knusdp.SmartLedger.dto.GoalResponseDto;
import com.knusdp.SmartLedger.dto.UpdateGoalRequestDto;
import com.knusdp.SmartLedger.service.GoalService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
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
    private final ObjectMapper objectMapper; // JSON 변환기 주입

    @Operation(summary = "목표 생성 (이미지 업로드 포함)")
    @PostMapping(consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<String> createGoal(
            // 변경점: DTO가 아니라 String으로 받음 (Content-Type 신경 안 써도 됨)
            @RequestPart("data") String data,

            @RequestPart(value = "image", required = false) MultipartFile image
    ) throws JsonProcessingException {

        // 1. 여기서 수동으로 String -> DTO 변환 (이러면 에러 안 남)
        CreateGoalRequestDto dto = objectMapper.readValue(data, CreateGoalRequestDto.class);
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        Long userId = Long.parseLong(authentication.getName());

        goalService.createGoal(userId, dto, image);

        return ResponseEntity.status(HttpStatus.CREATED).body("목표가 생성되었습니다");
    }

    // ... 아래 getGoals, updateGoal 등 다른 메서드는 그대로 두세요 ...
    @GetMapping
    public ResponseEntity<List<GoalResponseDto>> getGoals() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        Long userId = Long.parseLong(authentication.getName());
        List<GoalResponseDto> response = goalService.findGoalsByMemberId(userId);
        return ResponseEntity.ok(response);
    }

    // (나머지 코드 생략 - 기존과 동일)
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