package chainsawman.gesture.dto.friend.response;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Builder;
import lombok.Getter;

import java.time.LocalDateTime;

@Getter
@Builder
public class FriendDeleteResponse {

    private boolean deleted;

    @JsonProperty("user_idx")
    private Long userIdx;

    @JsonProperty("target_user_idx")
    private Long targetUserIdx;

    @JsonProperty("deleted_at")
    private LocalDateTime deletedAt;
}
