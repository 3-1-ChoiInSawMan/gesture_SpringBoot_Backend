package chainsawman.gesture.controller;

import chainsawman.gesture.dto.room.request.RoomJoinRequest;
import chainsawman.gesture.dto.room.request.RoomPatchRequest;
import chainsawman.gesture.dto.room.request.RoomRequest;
import chainsawman.gesture.dto.room.response.*;
import chainsawman.gesture.enums.RoomType;
import chainsawman.gesture.global.ApiResponse;
import chainsawman.gesture.service.RoomService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/rooms")
@RequiredArgsConstructor
@Tag(name = "Room", description = "통화방 관련 API")
public class RoomController {
    private final RoomService roomService;

    @PostMapping
    @Operation(summary = "통화방 생성", description = "통화방 생성 시 사용하는 API 입니다.")
    public ResponseEntity<ApiResponse<RoomResponse>> createRoom(@RequestBody @Valid RoomRequest request) {
        RoomResponse result = roomService.createRoom(request);
        return ResponseEntity.ok(ApiResponse.ok(result, "요청이 성공적으로 처리되었습니다."));
    }

    @GetMapping
    @Operation(summary = "통화방 목록 조회", description = "category 필터로 통화방 목록을 조회합니다.")
    public ResponseEntity<ApiResponse<Page<RoomListResponse>>> getRooms(
            Pageable pageable,
            @RequestParam(required = false) RoomType category) {
        Page<RoomListResponse> result = roomService.getRooms(pageable, category);
        return ResponseEntity.ok(ApiResponse.ok(result, "요청이 성공적으로 처리되었습니다."));
    }

    @GetMapping("/search")
    @Operation(summary = "통화방 검색", description = "제목 키워드와 category 필터로 통화방을 검색합니다.")
    public ResponseEntity<ApiResponse<Page<RoomListResponse>>> searchRooms(
            Pageable pageable,
            @RequestParam String keyword,
            @RequestParam(required = false) RoomType category) {
        Page<RoomListResponse> result = roomService.searchRooms(pageable, keyword, category);
        return ResponseEntity.ok(ApiResponse.ok(result, "요청이 성공적으로 처리되었습니다."));
    }

    @GetMapping("/{roomIdx}")
    @Operation(summary = "통화방 상세 조회", description = "통화방 상세 조회 시 사용하는 API 입니다.")
    public ResponseEntity<ApiResponse<RoomDetailResponse>> getRoomDetail(@PathVariable Long roomIdx) {
        RoomDetailResponse result = roomService.getRoomDetail(roomIdx);
        return ResponseEntity.ok(ApiResponse.ok(result, "조회되었습니다."));
    }

    @PatchMapping("/{roomIdx}")
    @Operation(summary = "통화방 수정", description = "방장만 가능합니다.")
    public ResponseEntity<ApiResponse<RoomPatchResponse>> patchRoom(
            @PathVariable Long roomIdx,
            @RequestBody RoomPatchRequest request) {
        RoomPatchResponse result = roomService.patchRoom(roomIdx, request);
        return ResponseEntity.ok(ApiResponse.ok(result, "수정되었습니다."));
    }

    @DeleteMapping("/{roomIdx}")
    @Operation(summary = "통화방 삭제", description = "통화방 삭제 시 사용하는 API 입니다.")
    public ResponseEntity<ApiResponse<RoomDeleteResponse>> deleteRoom(@PathVariable Long roomIdx) {
        RoomDeleteResponse result = roomService.deleteRoom(roomIdx);
        return ResponseEntity.ok(ApiResponse.ok(result, "요청이 성공적으로 처리되었습니다."));
    }

    @PostMapping("/{roomIdx}/join")
    @Operation(summary = "통화방 참여", description = "비공개방은 password 필수입니다.")
    public ResponseEntity<ApiResponse<RoomJoinResponse>> joinRoom(
            @PathVariable Long roomIdx,
            @RequestBody(required = false) RoomJoinRequest request) {
        RoomJoinResponse result = roomService.joinRoom(roomIdx, request);
        return ResponseEntity.ok(ApiResponse.ok(result, "요청이 성공적으로 처리되었습니다."));
    }

    @DeleteMapping("/{roomIdx}/leave")
    @Operation(summary = "통화방 나가기", description = "방장이 나가면 다음 멤버에게 방장이 위임됩니다. 마지막 멤버면 방이 삭제됩니다.")
    public ResponseEntity<ApiResponse<RoomLeaveResponse>> leaveRoom(@PathVariable Long roomIdx) {
        RoomLeaveResponse result = roomService.leaveRoom(roomIdx);
        return ResponseEntity.ok(ApiResponse.ok(result, "요청이 성공적으로 처리되었습니다."));
    }
}

