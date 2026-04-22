package chainsawman.gesture.dto.friend.response;

import chainsawman.gesture.entity.user.User;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
public class FriendListResponse {

    @JsonProperty("friend_idx")
    private Long friendIdx;

    @JsonProperty("friend_id")
    private String friendId;

    @JsonProperty("friend_nickname")
    private String friendNickname;

    public static FriendListResponse from(User friend) {
        return FriendListResponse.builder()
                .friendIdx(friend.getIdx())
                .friendId(friend.getId())
                .friendNickname(friend.getNickname())
                .build();
    }
}
