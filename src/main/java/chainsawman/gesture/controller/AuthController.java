package chainsawman.gesture.controller;

import chainsawman.gesture.dto.user.request.*;
import chainsawman.gesture.dto.user.response.*;
import chainsawman.gesture.global.ApiResponse;
import chainsawman.gesture.service.AuthService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/auth")
@RequiredArgsConstructor
@Tag(name = "Auth", description = "인증 관련 API")
public class AuthController {

    private final AuthService authService;

    @PostMapping("/login")
    @Operation(summary = "로그인 검증", description = "로그인 검증 시 사용하는 API 입니다.")
    public ResponseEntity<ApiResponse<LoginResponse>> login(@RequestBody @Valid LoginRequest request) {
        LoginResponse result = authService.login(request);
        return ResponseEntity.ok(ApiResponse.ok(result, "검증되었습니다."));
    }

    @PostMapping("/register")
    @Operation(summary = "회원가입", description = "유저 생성 시 사용하는 API 입니다.")
    public ResponseEntity<ApiResponse<RegisterResponse>> register(@RequestBody @Valid RegisterRequest request) {
        RegisterResponse result = authService.register(request);
        return ResponseEntity.ok(ApiResponse.ok(result, "요청이 성공적으로 처리되었습니다."));
    }

    @PostMapping("/social-login")
    @Operation(summary = "소셜 로그인 처리", description = "소셜 로그인 시 사용하는 API 입니다.")
    public ResponseEntity<ApiResponse<SocialLoginResponse>> socialLogin(@RequestBody @Valid SocialLoginRequest request) {
        SocialLoginResponse result = authService.socialLogin(request);
        return ResponseEntity.ok(ApiResponse.ok(result, "요청이 성공적으로 처리되었습니다."));
    }

    @PostMapping("/social-register")
    @Operation(summary = "소셜 회원가입 완료", description = "소셜로그인에서 신규 유저를 생성할 때 사용하는 API 입니다.")
    public ResponseEntity<ApiResponse<SocialRegisterResponse>> socialRegister(@RequestBody @Valid SocialRegisterRequest request) {
        SocialRegisterResponse result = authService.socialRegister(request);
        return ResponseEntity.ok(ApiResponse.ok(result, "유저가 생성되었습니다."));
    }

    @PostMapping("/refresh")
    @Operation(summary = "리프레시 토큰 검증", description = "자동 로그인 시 사용하는 API 입니다.")
    public ResponseEntity<ApiResponse<RefreshTokenValidationResponse>> refreshTokenValidation
            (@RequestBody @Valid RefreshTokenRequest request) {
        RefreshTokenValidationResponse result = authService.refreshTokenValidation(request);
        return ResponseEntity.ok(ApiResponse.ok(result, "검증되었습니다."));
    }

    @DeleteMapping("/refresh")
    @Operation(summary = "리프레시 토큰 삭제", description = "로그아웃 시 사용하는 API 입니다.")
    public ResponseEntity<ApiResponse<Void>> deleteRefreshToken(@RequestBody @Valid RefreshTokenRequest request) {
        authService.deleteRefreshToken(request);
        return ResponseEntity.ok(ApiResponse.ok("삭제되었습니다."));
    }

    @PostMapping("/email-send")
    @Operation(summary = "이메일 인증 발송", description = "이메일 인증 코드를 발송합니다.")
    public ResponseEntity<ApiResponse<Void>> sendEmailVerification(@RequestBody @Valid EmailSendRequest request) {
        authService.sendEmailVerification(request);
        return ResponseEntity.ok(ApiResponse.ok("인증 코드가 발송되었습니다."));
    }

    @PostMapping("/email-verification")
    @Operation(summary = "이메일 인증 확인", description = "이메일 인증 코드를 검증합니다.")
    public ResponseEntity<ApiResponse<EmailVerificationResponse>> verifyEmail(@RequestBody @Valid EmailVerificationRequest request) {
        EmailVerificationResponse result = authService.verifyEmail(request);
        return ResponseEntity.ok(ApiResponse.ok(result));
    }







}
