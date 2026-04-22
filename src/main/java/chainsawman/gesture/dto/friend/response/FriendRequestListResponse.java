package chainsawman.gesture.dto.friend.response;

import chainsawman.gesture.entity.friend.Friendship;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Builder;
import lombok.Getter;

import java.time.LocalDateTime;

@Getter
@Builder
public class FriendRequestListResponse {

    @JsonProperty("friend_request_idx")
    private Long friendRequestIdx;

    @JsonProperty("requester_idx")
    private Long requesterIdx;

    @JsonProperty("requester_id")
    private String requesterId;

    @JsonProperty("requester_nickname")
    private String requesterNickname;

    private String status;

    @JsonProperty("created_at")
    private LocalDateTime createdAt;

    public static FriendRequestListResponse from(Friendship friendship) {
        return FriendRequestListResponse.builder()
                .friendRequestIdx(friendship.getIdx())
                .requesterIdx(friendship.getUser().getIdx())
                .requesterId(friendship.getUser().getId())
                .requesterNickname(friendship.getUser().getNickname())
                .status(friendship.getStatus().name())
                .createdAt(friendship.getRequestAt())
                .build();
    }
}
