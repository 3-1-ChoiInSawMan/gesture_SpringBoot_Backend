package chainsawman.gesture.controller;

import chainsawman.gesture.dto.chatRoom.request.ChatRoomInviteRespondRequest;
import chainsawman.gesture.dto.chatRoom.request.ChatRoomRequest;
import chainsawman.gesture.dto.chatRoom.request.ReadMarkRequest;
import chainsawman.gesture.dto.chatRoom.request.SendMessageRequest;
import chainsawman.gesture.dto.chatRoom.response.*;
import chainsawman.gesture.global.ApiResponse;
import chainsawman.gesture.service.ChatRoomService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/chat-rooms")
@RequiredArgsConstructor
@Tag(name = "ChatRoom", description = "채팅방 관련 API")
public class ChatRoomController {

    private final ChatRoomService chatRoomService;

    @PostMapping
    @Operation(summary = "채팅방 생성", description = "채팅방 생성 시 사용하는 API 입니다.")
    public ResponseEntity<ApiResponse<ChatRoomResponse>> createChatRoom(@RequestBody @Valid ChatRoomRequest request) {
        ChatRoomResponse result = chatRoomService.createChatRoom(request);
        return ResponseEntity.ok(ApiResponse.ok(result, "채팅방이 생성되었습니다."));
    }

    @PatchMapping("/invitations/{invitationIdx}")
    @Operation(summary = "채팅방 초대 수락/거절", description = "accept: true면 수락, false면 거절입니다.")
    public ResponseEntity<ApiResponse<ChatRoomInviteRespondResponse>> respondToInvitation(
            @PathVariable Long invitationIdx,
            @RequestBody ChatRoomInviteRespondRequest request) {
        ChatRoomInviteRespondResponse result = chatRoomService.respondToInvitation(invitationIdx, request);
        return ResponseEntity.ok(ApiResponse.ok(result, "처리되었습니다."));
    }

    @GetMapping
    @Operation(summary = "내 채팅방 목록 조회", description = "내가 참여 중인 채팅방 목록을 조회합니다.")
    public ResponseEntity<ApiResponse<List<ChatRoomListResponse>>> getMyChatRooms() {
        List<ChatRoomListResponse> result = chatRoomService.getMyChatRooms();
        return ResponseEntity.ok(ApiResponse.ok(result, "채팅방 목록 조회에 성공했습니다."));
    }

    @GetMapping("/{chatRoomIdx}")
    @Operation(summary = "채팅방 상세 조회", description = "채팅방 상세 정보 및 참여자 목록을 조회합니다.")
    public ResponseEntity<ApiResponse<ChatRoomDetailResponse>> getChatRoomDetail(@PathVariable Long chatRoomIdx) {
        ChatRoomDetailResponse result = chatRoomService.getChatRoomDetail(chatRoomIdx);
        return ResponseEntity.ok(ApiResponse.ok(result, "채팅방 상세 조회에 성공했습니다."));
    }

    @DeleteMapping("/{chatRoomIdx}/leave")
    @Operation(summary = "채팅방 나가기", description = "채팅방에서 나갑니다.")
    public ResponseEntity<ApiResponse<Void>> leaveChatRoom(@PathVariable Long chatRoomIdx) {
        chatRoomService.leaveChatRoom(chatRoomIdx);
        return ResponseEntity.ok(ApiResponse.ok("채팅방에서 나갔습니다."));
    }

    @GetMapping("/{chatRoomIdx}/messages")
    @Operation(summary = "메시지 목록 조회", description = "cursor 기반 페이징으로 메시지를 조회합니다. cursor가 없으면 최신 메시지부터, 있으면 해당 idx보다 이전 메시지를 반환합니다.")
    public ResponseEntity<ApiResponse<ChatMessageListResponse>> getMessages(
            @PathVariable Long chatRoomIdx,
            @RequestParam(required = false) Long cursor,
            @RequestParam(defaultValue = "20") int size) {
        ChatMessageListResponse result = chatRoomService.getMessages(chatRoomIdx, cursor, size);
        return ResponseEntity.ok(ApiResponse.ok(result, "메시지 목록 조회에 성공했습니다."));
    }

    @PostMapping("/{chatRoomIdx}/messages")
    @Operation(summary = "메시지 전송", description = "채팅방에 메시지를 전송합니다. TEXT 타입은 message 필드, FILE 타입은 file_uuid 필드를 사용합니다.")
    public ResponseEntity<ApiResponse<ChatMessageResponse>> sendMessage(
            @PathVariable Long chatRoomIdx,
            @RequestBody @Valid SendMessageRequest request) {
        ChatMessageResponse result = chatRoomService.sendMessage(chatRoomIdx, request);
        return ResponseEntity.ok(ApiResponse.ok(result, "메시지가 전송되었습니다."));
    }

    @PatchMapping("/{chatRoomIdx}/read")
    @Operation(summary = "읽음 처리", description = "마지막으로 읽은 메시지 idx를 업데이트합니다.")
    public ResponseEntity<ApiResponse<ReadMarkResponse>> markAsRead(
            @PathVariable Long chatRoomIdx,
            @RequestBody @Valid ReadMarkRequest request) {
        ReadMarkResponse result = chatRoomService.markAsRead(chatRoomIdx, request);
        return ResponseEntity.ok(ApiResponse.ok(result, "읽음 처리되었습니다."));
    }
}
