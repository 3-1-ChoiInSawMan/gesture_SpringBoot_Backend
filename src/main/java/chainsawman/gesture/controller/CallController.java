package chainsawman.gesture.controller;

import chainsawman.gesture.dto.call.response.CallJoinResponse;
import chainsawman.gesture.dto.call.response.CallLeaveResponse;
import chainsawman.gesture.dto.call.response.CallParticipantsResponse;
import chainsawman.gesture.global.ApiResponse;
import chainsawman.gesture.service.CallService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/calls")
@RequiredArgsConstructor
@Tag(name = "Call", description = "실시간 통화 관련 API")
public class CallController {

    private final CallService callService;

    @PostMapping("/{roomIdx}/join")
    @Operation(summary = "통화 참여", description = "통화방에 참여합니다. 활성 세션이 없으면 새 세션을 생성합니다.")
    public ResponseEntity<ApiResponse<CallJoinResponse>> joinCall(@PathVariable Long roomIdx) {
        CallJoinResponse result = callService.joinCall(roomIdx);
        return ResponseEntity.ok(ApiResponse.ok(result, "요청이 성공적으로 처리되었습니다."));
    }

    @PostMapping("/{roomIdx}/leave")
    @Operation(summary = "통화 나가기", description = "통화방에서 나갑니다. 마지막 참여자가 나가면 세션이 종료됩니다.")
    public ResponseEntity<ApiResponse<CallLeaveResponse>> leaveCall(@PathVariable Long roomIdx) {
        CallLeaveResponse result = callService.leaveCall(roomIdx);
        return ResponseEntity.ok(ApiResponse.ok(result, "요청이 성공적으로 처리되었습니다."));
    }

    @GetMapping("/{roomIdx}/participants")
    @Operation(summary = "참가자 조회", description = "현재 통화 중인 참가자 목록을 조회합니다.")
    public ResponseEntity<ApiResponse<CallParticipantsResponse>> getParticipants(@PathVariable Long roomIdx) {
        CallParticipantsResponse result = callService.getParticipants(roomIdx);
        return ResponseEntity.ok(ApiResponse.ok(result, "요청이 성공적으로 처리되었습니다."));
    }
}
