package com.example.emotion_storage.test;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

@Tag(name = "테스트", description = "테스트 API")
@RestController
public class TestController {

    @Operation(summary = "테스트 API", description = "Hello, mooi String을 반환합니다.")
    @GetMapping("/hello")
    public String hello() {
        return "Hello, mooi!";
    }
}
