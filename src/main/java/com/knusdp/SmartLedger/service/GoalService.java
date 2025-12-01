package com.knusdp.SmartLedger.service;

import com.knusdp.SmartLedger.dto.goal.CreateGoalRequestDto;
import com.knusdp.SmartLedger.dto.goal.GoalResponseDto;
import com.knusdp.SmartLedger.dto.UpdateGoalRequestDto;
import com.knusdp.SmartLedger.entity.Goal;
import com.knusdp.SmartLedger.entity.GoalStatus;
import com.knusdp.SmartLedger.entity.Member;
import com.knusdp.SmartLedger.exception.GoalNotFoundException;
import com.knusdp.SmartLedger.exception.UserNotFoundException;
import com.knusdp.SmartLedger.exception.asset.InvalidDateRangeException;
import com.knusdp.SmartLedger.repository.GoalRepository;
import com.knusdp.SmartLedger.repository.MemberRepository;
import jakarta.validation.ConstraintViolation;
import jakarta.validation.Validator;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.io.IOException;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

@RequiredArgsConstructor
@Service
public class GoalService {
    private final MemberRepository memberRepository;
    private final GoalRepository goalRepository;
    private final S3UploadService s3UploadService;
    private final Validator validator;

    @Transactional
    public Long createGoal(Long userId, CreateGoalRequestDto dto) {

        // 1. 사용자 검증 — 인증 실패 처리 포함
        Member member = memberRepository.findById(userId)
                .orElseThrow(() -> new UserNotFoundException("로그인이 필요합니다."));

        // 2. DTO 값 수동 검증 (업로드 파일 포함 검증)
        Set<ConstraintViolation<CreateGoalRequestDto>> violations =
                validator.validate(dto);

        if (!violations.isEmpty()) {
            String errorMessage = violations.iterator().next().getMessage();
            throw new IllegalArgumentException(errorMessage);
        }

        if (dto.getStartDate() != null && dto.getDeadline() != null) {
            if (dto.getStartDate().isAfter(dto.getDeadline())) {
                throw new InvalidDateRangeException("시작일은 마감일보다 늦을 수 없습니다.");
            }
        }

        // 3. 이미지 업로드(있을 때만)
        String imageUrl = null;
        if (dto.getImage() != null && !dto.getImage().isEmpty()) {
            try {
                imageUrl = s3UploadService.saveFile(dto.getImage());
            } catch (IOException e) {
                throw new RuntimeException("이미지 업로드에 실패했습니다.", e);
            }
        }

        // 4. 엔티티 생성
        Goal goal = Goal.builder()
                .title(dto.getTitle())
                .targetAmount(dto.getTargetAmount())
                .startDate(dto.getStartDate())
                .deadline(dto.getDeadline())
                .imageUrl(imageUrl)
                .member(member)
                .build();

        goalRepository.save(goal);

        return goal.getGoalId();
    }



    public List<GoalResponseDto> findGoalsByMemberId(Long memberId) {
        // 1. 리포지토리 호출하여 특정 사용자의 목표 목록 조회
        List<Goal> goals = goalRepository.findByMember_IdOrderByCreatedAtDesc(memberId);

        // 2. 조회된 엔티티 목록을 DTO 목록으로 변환
        return goals.stream()
                .map(GoalResponseDto::new) // 엔티티를 DTO로 변환하면서 진행률 계산
                .collect(Collectors.toList());
    }
    public GoalResponseDto findGoalById(Long memberId, Long goalId) {
        // 1. 리포지토리 호출 (본인 소유의 목표인지 함께 확인)
        Goal goal = goalRepository.findByMember_IdAndGoalId(memberId, goalId)
                .orElseThrow(() -> new GoalNotFoundException("해당 목표를 찾을 수 없습니다."));

        // 2. 조회된 엔티티를 DTO로 변환하여 반환
        return new GoalResponseDto(goal);
    }
    @Transactional // 데이터 변경 작업
    public GoalResponseDto updateGoal(Long memberId, Long goalId, UpdateGoalRequestDto dto) {
        // 1. 본인 소유의 목표인지 확인하며 엔티티 조회
        Goal goalToUpdate = goalRepository.findByMember_IdAndGoalId(memberId, goalId)
                .orElseThrow(() -> new GoalNotFoundException("해당 목표를 찾을 수 없습니다."));


        if (dto.getStartDate() != null && dto.getDeadline() != null) {
            if (dto.getStartDate().isAfter(dto.getDeadline())) {
                throw new InvalidDateRangeException("시작일은 마감일보다 늦을 수 없습니다.");
            }
        }

        // 2. DTO에 값이 있는 필드만 선택적으로 업데이트
        if (dto.getTitle() != null) {
            goalToUpdate.setTitle(dto.getTitle());
        }
        if (dto.getImageUrl() != null) {
            goalToUpdate.setImageUrl(dto.getImageUrl());
        }
        if (dto.getTargetAmount() != null) {
            goalToUpdate.setTargetAmount(dto.getTargetAmount());
        }
        if (dto.getCurrentAmount() != null) {
            goalToUpdate.setCurrentAmount(dto.getCurrentAmount());
        }
        if (dto.getStartDate() != null) {
            goalToUpdate.setDeadline(dto.getDeadline());
        }
        if (dto.getDeadline() != null) {
            goalToUpdate.setDeadline(dto.getDeadline());
        }
        if (dto.getStatus() != null) {
            goalToUpdate.setStatus(dto.getStatus());
        }

        // 3. (자동 상태 변경 로직) 현재 금액이 목표 금액 이상이면 완료 상태로 변경
        //    단, DTO에서 명시적으로 다른 상태를 지정하지 않았을 경우
        if (dto.getStatus() == null &&
                goalToUpdate.getCurrentAmount().compareTo(goalToUpdate.getTargetAmount()) >= 0) {
            goalToUpdate.setStatus(GoalStatus.COMPLETED);
        }


        // @Transactional에 의해 변경된 내용이 자동으로 DB에 저장됨 (Dirty Checking)
        // DTO로 변환하여 반환 (업데이트된 updatedAt 시간 포함)
        return new GoalResponseDto(goalToUpdate);
    }
    @Transactional // 데이터 변경 작업
    public void deleteGoal(Long memberId, Long goalId) {
        // 1. 본인 소유의 목표인지 확인하며 엔티티 조회
        Goal goalToDelete = goalRepository.findByMember_IdAndGoalId(memberId, goalId)
                .orElseThrow(() -> new GoalNotFoundException("해당 목표를 찾을 수 없습니다."));

        // 2. 조회된 엔티티 삭제
        goalRepository.delete(goalToDelete);
    }
}
