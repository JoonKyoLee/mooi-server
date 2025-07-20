package com.example.emotion_storage.test;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

@Slf4j
@RestController
@RequestMapping("/s3")
@RequiredArgsConstructor
public class S3TestController {

    private final S3TestService testS3Service;

    @PostMapping("/upload")
    public String uploadImage(@RequestParam("file") MultipartFile file) {
        try {
            String imageUrl = testS3Service.uploadImage(file);
            return "파일이 성공적으로 업로드 되었습니다. URL: " + imageUrl;
        } catch (Exception e) {
            log.error("파일 업로드 중 오류 발생", e);
            return "파일 업로드 중 오류가 발생했습니다: " + e.getMessage();
        }
    }
}
