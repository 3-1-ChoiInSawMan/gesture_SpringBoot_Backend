package chainsawman.gesture.dto.chatRoom.response;

import chainsawman.gesture.entity.chat.ChatRoomInvitation;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
public class ChatRoomInviteRespondResponse {

    @JsonProperty("invitation_idx")
    private Long invitationIdx;

    @JsonProperty("chat_room_idx")
    private Long chatRoomIdx;

    private String status;

    public static ChatRoomInviteRespondResponse from(ChatRoomInvitation invitation) {
        return ChatRoomInviteRespondResponse.builder()
                .invitationIdx(invitation.getIdx())
                .chatRoomIdx(invitation.getChatRoom().getIdx())
                .status(invitation.getStatus().name())
                .build();
    }
}
