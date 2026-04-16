package chainsawman.gesture.controller;

import chainsawman.gesture.dto.user.request.LoginRequest;
import chainsawman.gesture.dto.user.request.RegisterRequest;
import chainsawman.gesture.dto.user.response.LoginResponse;
import chainsawman.gesture.dto.user.response.RegisterResponse;
import chainsawman.gesture.global.ApiResponse;
import chainsawman.gesture.service.AuthService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/auth")
@RequiredArgsConstructor
@Tag(name = "Auth", description = "인증 관련 API")
public class AuthController {

    private final AuthService authService;

    @PostMapping("/login")
    @Operation(summary = "로그인 검증", description = "로그인 검증 시 사용하는 API 입니다.")
    public ResponseEntity<ApiResponse<LoginResponse>> login(@RequestBody LoginRequest request) {
        LoginResponse result = authService.login(request);
        return ResponseEntity.ok(ApiResponse.ok(result, "검증되었습니다."));
    }

    @PostMapping("/register")
    @Operation(summary = "회원가입", description = "유저 생성 시 사용하는 API 입니다.")
    public ResponseEntity<ApiResponse<RegisterResponse>> register(@RequestBody RegisterRequest request) {
        RegisterResponse result = authService.register(request);
        return ResponseEntity.ok(ApiResponse.ok(result, "요청이 성공적으로 처리되었습니다."));
    }







}
