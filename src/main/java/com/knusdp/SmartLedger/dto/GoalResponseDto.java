package com.knusdp.SmartLedger.dto;

import com.knusdp.SmartLedger.entity.Goal;
import com.knusdp.SmartLedger.entity.GoalStatus;
import lombok.Getter;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDate;
import java.time.LocalDateTime; // LocalDateTime import

@Getter
public class GoalResponseDto {
    private Long goalId;
    private String title;
    private BigDecimal targetAmount;
    private BigDecimal currentAmount;
    private LocalDate deadline;
    private BigDecimal progressRate;
    private String imageUrl;
    private GoalStatus status;
    private LocalDateTime createAt; // 필드명 일치 (createdAt)
    private LocalDateTime updateAt; // 필드명 일치 (updatedAt)

    public GoalResponseDto(Goal goal) {
        this.goalId = goal.getGoalId();
        this.title = goal.getTitle();
        this.targetAmount = goal.getTargetAmount();
        this.currentAmount = goal.getCurrentAmount();
        this.deadline = goal.getDeadline();
        this.imageUrl = goal.getImageUrl();
        this.status = goal.getStatus();
        this.createAt = goal.getCreatedAt(); // createdAt 할당
        this.updateAt = goal.getUpdatedAt(); // updatedAt 할당

        if (goal.getTargetAmount() != null && goal.getTargetAmount().compareTo(BigDecimal.ZERO) > 0) {
            this.progressRate = goal.getCurrentAmount()
                    .divide(goal.getTargetAmount(), 2, RoundingMode.HALF_UP);
        } else {
            if (goal.getCurrentAmount() != null && goal.getCurrentAmount().compareTo(BigDecimal.ZERO) == 0) {
                this.progressRate = BigDecimal.ONE;
            } else {
                this.progressRate = BigDecimal.ZERO;
            }
        }
    }
}