package com.knusdp.SmartLedger.dto;

import lombok.Data;
import org.springframework.web.multipart.MultipartFile;

@Data
public class GoalRequestSchema {
    private CreateGoalRequestDto data;
    private MultipartFile image;
}

