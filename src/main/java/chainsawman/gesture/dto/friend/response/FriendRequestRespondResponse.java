package chainsawman.gesture.dto.friend.response;

import chainsawman.gesture.entity.friend.Friendship;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Builder;
import lombok.Getter;

import java.time.LocalDateTime;

@Getter
@Builder
public class FriendRequestRespondResponse {

    @JsonProperty("friend_request_idx")
    private Long friendRequestIdx;

    @JsonProperty("requester_idx")
    private Long requesterIdx;

    @JsonProperty("receiver_idx")
    private Long receiverIdx;

    private String status;

    @JsonProperty("created_at")
    private LocalDateTime createdAt;

    public static FriendRequestRespondResponse from(Friendship friendship) {
        return FriendRequestRespondResponse.builder()
                .friendRequestIdx(friendship.getIdx())
                .requesterIdx(friendship.getUser().getIdx())
                .receiverIdx(friendship.getFriend().getIdx())
                .status(friendship.getStatus().name())
                .createdAt(friendship.getRequestAt())
                .build();
    }
}
