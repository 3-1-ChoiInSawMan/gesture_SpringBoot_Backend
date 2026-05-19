package chainsawman.gesture.dto.friend.request;

import com.fasterxml.jackson.annotation.JsonProperty;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor
@AllArgsConstructor
public class FriendInviteRequest {

    @NotNull
    @JsonProperty("target_user_idx")
    private Long targetUserIdx;

    @NotNull
    @JsonProperty("target_room_idx")
    private Long targetRoomIdx;
}
