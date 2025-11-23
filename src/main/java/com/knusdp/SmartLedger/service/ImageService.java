package com.knusdp.SmartLedger.service;

import com.knusdp.SmartLedger.dto.ImageUpLoadDto;
import com.knusdp.SmartLedger.entity.Image;
import com.knusdp.SmartLedger.repository.ImageRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.io.IOException;

@Service
@RequiredArgsConstructor
public class ImageService {

    private final S3UploadService s3UploadService;
    private final ImageRepository imageRepository;

    public String uploadCoverImage(ImageUpLoadDto dto) throws IOException {

        // 1. S3에 이미지 업로드
        String imageUrl = s3UploadService.saveFile(dto.getFile());

        // 2. DB 저장
        Image image = dto.toEntity(imageUrl);
        Image savedImage = imageRepository.save(image);

        return savedImage.getImageUrl();
    }
}

