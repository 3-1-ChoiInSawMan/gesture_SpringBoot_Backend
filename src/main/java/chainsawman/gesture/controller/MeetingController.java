package chainsawman.gesture.controller;

import chainsawman.gesture.dto.meeting.request.MeetingMinutesCreateRequest;
import chainsawman.gesture.dto.meeting.request.MeetingMinutesUpdateRequest;
import chainsawman.gesture.dto.meeting.response.*;
import chainsawman.gesture.global.ApiResponse;
import chainsawman.gesture.service.MeetingService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequiredArgsConstructor
@Tag(name = "Meeting", description = "회의록 관련 API")
public class MeetingController {

    private final MeetingService meetingService;

    @PostMapping("/calls/{callIdx}/minutes/start")
    @Operation(summary = "회의록 시작", description = "통화 세션에 대한 회의록을 시작합니다.")
    public ResponseEntity<ApiResponse<MeetingStartResponse>> startMinutes(@PathVariable Long callIdx) {
        MeetingStartResponse result = meetingService.startMinutes(callIdx);
        return ResponseEntity.ok(ApiResponse.ok(result, "회의록이 시작되었습니다."));
    }

    @PostMapping("/calls/{callIdx}/minutes")
    @Operation(summary = "회의록 생성", description = "BFF 또는 Redis Stream 컨슈머에서 AI 처리 완료 후 호출합니다.")
    public ResponseEntity<ApiResponse<MeetingDetailResponse>> createMinutes(
            @PathVariable Long callIdx,
            @RequestBody MeetingMinutesCreateRequest request) {
        MeetingDetailResponse result = meetingService.createMinutes(callIdx, request);
        return ResponseEntity.ok(ApiResponse.ok(result, "회의록이 저장되었습니다."));
    }

    @PostMapping("/calls/{callIdx}/minutes/end")
    @Operation(summary = "회의록 종료 및 요약", description = "진행 중인 회의록을 종료하고 최종 데이터를 반환합니다.")
    public ResponseEntity<ApiResponse<MeetingEndResponse>> endMinutes(@PathVariable Long callIdx) {
        MeetingEndResponse result = meetingService.endMinutes(callIdx);
        return ResponseEntity.ok(ApiResponse.ok(result, "회의록이 종료되었습니다."));
    }

    @GetMapping("/rooms/{roomIdx}/minutes")
    @Operation(summary = "그룹방 회의록 목록 조회", description = "해당 그룹방의 전체 회의록 목록을 조회합니다.")
    public ResponseEntity<ApiResponse<List<MeetingListItemResponse>>> getMinutesList(@PathVariable Long roomIdx) {
        List<MeetingListItemResponse> result = meetingService.getMinutesList(roomIdx);
        return ResponseEntity.ok(ApiResponse.ok(result, "조회되었습니다."));
    }

    @GetMapping("/meetings/{minutesIdx}")
    @Operation(summary = "회의록 단건 조회", description = "회의록 단건 조회 시 사용하는 API 입니다.")
    public ResponseEntity<ApiResponse<MeetingDetailResponse>> getMinutesDetail(@PathVariable Long minutesIdx) {
        MeetingDetailResponse result = meetingService.getMinutesDetail(minutesIdx);
        return ResponseEntity.ok(ApiResponse.ok(result, "조회되었습니다."));
    }

    @PatchMapping("/meetings/{minutesIdx}")
    @Operation(summary = "회의록 수정", description = "회의록 제목, 내용, 결론을 수정합니다.")
    public ResponseEntity<ApiResponse<MeetingDetailResponse>> updateMinutes(
            @PathVariable Long minutesIdx,
            @RequestBody MeetingMinutesUpdateRequest request) {
        MeetingDetailResponse result = meetingService.updateMinutes(minutesIdx, request);
        return ResponseEntity.ok(ApiResponse.ok(result, "수정되었습니다."));
    }

    @DeleteMapping("/meetings/{minutesIdx}")
    @Operation(summary = "회의록 삭제", description = "회의록 삭제 시 사용하는 API 입니다.")
    public ResponseEntity<ApiResponse<Void>> deleteMinutes(@PathVariable Long minutesIdx) {
        meetingService.deleteMinutes(minutesIdx);
        return ResponseEntity.ok(ApiResponse.ok("회의록이 삭제되었습니다."));
    }
}
