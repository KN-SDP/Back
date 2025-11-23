package com.knusdp.SmartLedger.dto;

import com.knusdp.SmartLedger.entity.Image;
import lombok.Data;
import org.springframework.web.multipart.MultipartFile;

@Data
public class ImageUpLoadDto {
    private MultipartFile file;
    private String title;

    public Image toEntity(String imageUrl) {
        return Image.builder()
                .imageUrl(imageUrl)
                .title(title)
                .build();
    }
}
