package chainsawman.gesture.dto.friend.response;

import chainsawman.gesture.entity.friend.FriendInvite;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;

import java.time.LocalDateTime;

@Getter
@AllArgsConstructor
@Builder
public class FriendInviteResponse {

    @JsonProperty("invite_idx")
    private Long inviteIdx;

    @JsonProperty("sender_idx")
    private Long senderIdx;

    @JsonProperty("receiver_idx")
    private Long receiverIdx;

    @JsonProperty("room_idx")
    private Long roomIdx;

    private String message;

    @JsonProperty("created_at")
    private LocalDateTime createdAt;

    public static FriendInviteResponse from(FriendInvite invite) {
        return FriendInviteResponse.builder()
                .inviteIdx(invite.getIdx())
                .senderIdx(invite.getSender().getIdx())
                .receiverIdx(invite.getReceiver().getIdx())
                .roomIdx(invite.getRoom().getIdx())
                .message(invite.getSender().getNickname() + "님이 통화방에 초대했습니다.")
                .createdAt(invite.getCreatedAt())
                .build();
    }
}
