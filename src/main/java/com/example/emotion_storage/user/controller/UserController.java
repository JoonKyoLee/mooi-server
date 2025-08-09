package com.example.emotion_storage.user.controller;

import com.example.emotion_storage.global.api.ApiResponse;
import com.example.emotion_storage.global.api.SuccessMessage;
import com.example.emotion_storage.user.dto.request.GoogleLoginRequest;
import com.example.emotion_storage.user.dto.request.GoogleSignUpRequest;
import com.example.emotion_storage.user.dto.response.LoginResponse;
import com.example.emotion_storage.user.dto.response.SignupResponse;
import com.example.emotion_storage.user.service.UserService;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@Slf4j
@RestController
@RequestMapping("/api/v1/users")
@RequiredArgsConstructor
public class UserController {

    private final UserService userService;

    @PostMapping("/login/google")
    public ApiResponse<LoginResponse> loginWithGoogle(
            @RequestBody GoogleLoginRequest request, HttpServletResponse httpServletResponse) {
        LoginResponse response = userService.googleLogin(request, httpServletResponse);
        return ApiResponse.success(HttpStatus.CREATED.value(), SuccessMessage.LOGIN_SUCCESS.getMessage(), response);
    }

    @PostMapping("/signup/google")
    public ApiResponse<SignupResponse> signupWithGoogle(@RequestBody GoogleSignUpRequest request) {
        userService.googleSignUp(request);
        return ApiResponse.success(HttpStatus.CREATED.value(), SuccessMessage.SIGNUP_SUCCESS.getMessage(), SignupResponse.ok());
    }
}
