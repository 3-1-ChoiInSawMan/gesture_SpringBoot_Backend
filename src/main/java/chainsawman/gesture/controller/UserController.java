package chainsawman.gesture.controller;

import chainsawman.gesture.global.ApiResponse;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/users")
@RequiredArgsConstructor
@Tag(name = "Auth", description = "인증 관련 API")
public class UserController {

//    @GetMapping("/{userId}")
//    @Operation(summary = "프로필 조회", description = "유저 프로필 조회 시 사용하는 API 입니다.")
//    public ResponseEntity<ApiResponse<>>

}
