package chainsawman.gesture.controller;

import chainsawman.gesture.dto.friend.request.FriendInviteRequest;
import chainsawman.gesture.dto.friend.request.FriendRequestRespondRequest;
import chainsawman.gesture.dto.friend.response.*;
import chainsawman.gesture.global.ApiResponse;
import chainsawman.gesture.service.FriendService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/friends")
@RequiredArgsConstructor
@Tag(name = "Friend", description = "친구 관련 API")
public class FriendController {

    private final FriendService friendService;

    @PostMapping("/{userIdx}")
    @Operation(summary = "친구 요청 발송", description = "친구 요청 발송 시 사용하는 API 입니다.")
    public ResponseEntity<ApiResponse<FriendRequestSendResponse>> postFriend(@PathVariable Long userIdx) {
        FriendRequestSendResponse result = friendService.postFriend(userIdx);
        return ResponseEntity.ok(ApiResponse.ok(result, "친구 요청이 발송되었습니다."));
    }

    @GetMapping("/list")
    @Operation(summary = "친구 목록 조회", description = "ACCEPTED 상태인 친구 전체 목록을 조회합니다.")
    public ResponseEntity<ApiResponse<List<FriendListResponse>>> getFriendList() {
        List<FriendListResponse> result = friendService.getFriendList();
        return ResponseEntity.ok(ApiResponse.ok(result, "조회되었습니다."));
    }

    @GetMapping("/count")
    @Operation(summary = "친구 수 조회", description = "ACCEPTED 상태인 친구 수를 조회합니다.")
    public ResponseEntity<ApiResponse<FriendCountResponse>> getFriendCount() {
        FriendCountResponse result = friendService.getFriendCount();
        return ResponseEntity.ok(ApiResponse.ok(result, "조회되었습니다."));
    }

    @GetMapping
    @Operation(summary = "친구 요청 목록 조회", description = "친구 요청 목록 조회 시 사용하는 API 입니다.")
    public ResponseEntity<ApiResponse<List<FriendRequestListResponse>>> getFriendRequests() {
        List<FriendRequestListResponse> result = friendService.getFriendRequests();
        return ResponseEntity.ok(ApiResponse.ok(result, "조회되었습니다."));
    }

    @PatchMapping("/{friendshipIdx}")
    @Operation(summary = "친구 요청 수락/거절", description = "ACCEPTED or REJECTED로 선택합니다.")
    public ResponseEntity<ApiResponse<FriendRequestRespondResponse>> respondToFriendRequest(
            @PathVariable Long friendshipIdx,
            @RequestBody @Valid FriendRequestRespondRequest request) {
        FriendRequestRespondResponse result = friendService.respondToFriendRequest(friendshipIdx, request);
        return ResponseEntity.ok(ApiResponse.ok(result, "처리되었습니다."));
    }

    @DeleteMapping("/{userIdx}")
    @Operation(summary = "친구 삭제", description = "친구 삭제 시 사용하는 API 입니다.")
    public ResponseEntity<ApiResponse<FriendDeleteResponse>> deleteFriend(@PathVariable Long userIdx) {
        FriendDeleteResponse result = friendService.deleteFriend(userIdx);
        return ResponseEntity.ok(ApiResponse.ok(result, "친구가 삭제되었습니다."));
    }

    @PostMapping("/invite")
    @Operation(summary = "친구 통화방 초대", description = "친구 통화방 초대 시 사용하는 API 입니다.")
    public ResponseEntity<ApiResponse<FriendInviteResponse>> inviteFriend(@RequestBody @Valid FriendInviteRequest request) {
        FriendInviteResponse result = friendService.invite(request);
        return ResponseEntity.ok(ApiResponse.ok(result, "초대되었습니다."));
    }
}
