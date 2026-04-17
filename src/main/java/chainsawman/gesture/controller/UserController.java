package chainsawman.gesture.controller;

import chainsawman.gesture.dto.user.request.PatchMyProfileRequest;
import chainsawman.gesture.dto.user.request.PatchPasswordRequest;
import chainsawman.gesture.dto.user.response.*;
import chainsawman.gesture.global.ApiResponse;
import chainsawman.gesture.service.UserService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import lombok.With;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/users")
@RequiredArgsConstructor
@Tag(name = "User", description = "유저 관련 API")
public class UserController {

    private final UserService userService;

    @GetMapping("/{userIdx}")
    @Operation(summary = "프로필 조회", description = "유저 프로필 조회 시 사용하는 API 입니다.")
    public ResponseEntity<ApiResponse<ProfileResponse>> getProfile(@PathVariable Long userIdx) {
        ProfileResponse result = userService.getProfile(userIdx);
        return ResponseEntity.ok(ApiResponse.ok(result, "조회되었습니다."));
    }

    @GetMapping("/me")
    @Operation(summary = "내 프로필 조회", description = "내 프로필 조회 시 사용하는 API 입니다.")
    public ResponseEntity<ApiResponse<MyProfileResponse>> getMyProfile() {
        MyProfileResponse result = userService.getMyProfile();
        return ResponseEntity.ok(ApiResponse.ok(result, "조회되었습니다."));
    }

    @PatchMapping("/me")
    @Operation(summary = "내 프로필 수정", description = "내 프로필 수정 시 사용하는 API 입니다.")
    public ResponseEntity<ApiResponse<PatchMyProfileResponse>> patchMyProfile
            (@RequestBody PatchMyProfileRequest request) {
        PatchMyProfileResponse result = userService.patchMyProfile(request);
        return ResponseEntity.ok(ApiResponse.ok(result, "수정되었습니다."));
    }

    @PatchMapping("/password")
    @Operation(summary = "비밀번호 변경", description = "비밀번호 변경 시 사용하는 API 입니다.")
    public ResponseEntity<ApiResponse<PatchPasswordResponse>> patchPassword
            (@RequestBody PatchPasswordRequest request) {
        PatchPasswordResponse result = userService.patchPassword(request);
        return ResponseEntity.ok(ApiResponse.ok(result, "변경되었습니다."));
    }
    @DeleteMapping("/withdraw")
    @Operation(summary = "유저 삭제", description = "회원 탈퇴 시 사용하는 API 입니다.")
    public ResponseEntity<ApiResponse<WithdrawResponse>> deleteUser() {
        WithdrawResponse result = userService.deleteUser();
        return ResponseEntity.ok(ApiResponse.ok(result, "삭제되었습니다."));
    }

}
