package chainsawman.gesture.controller;

import chainsawman.gesture.dto.chatRoom.request.ChatRoomInviteRespondRequest;
import chainsawman.gesture.dto.chatRoom.request.ChatRoomRequest;
import chainsawman.gesture.dto.chatRoom.response.ChatRoomInviteRespondResponse;
import chainsawman.gesture.dto.chatRoom.response.ChatRoomResponse;
import chainsawman.gesture.global.ApiResponse;
import chainsawman.gesture.service.ChatRoomService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

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
}
