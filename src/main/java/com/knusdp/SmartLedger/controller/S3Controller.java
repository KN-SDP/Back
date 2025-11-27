package com.knusdp.SmartLedger.controller;

import com.knusdp.SmartLedger.dto.ImageUpLoadDto;
import com.knusdp.SmartLedger.service.ImageService;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.io.IOException;

@RestController
@RequiredArgsConstructor
@RequestMapping("/S3")
@SecurityRequirement(name = "bearerAuth")
public class S3Controller {

    private final ImageService imageService;

    @PostMapping("/upload")
    public String coverImageUpload(ImageUpLoadDto dto) throws IOException {

        String imageUrl = imageService.uploadCoverImage(dto);

        System.out.println("업로드된 이미지 URL: " + imageUrl);

        return imageUrl;   // 필요하면 JSON으로 반환 가능
    }
}

