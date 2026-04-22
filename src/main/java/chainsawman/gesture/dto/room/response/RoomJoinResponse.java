package chainsawman.gesture.dto.room.response;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Builder;
import lombok.Getter;

import java.time.LocalDateTime;

@Getter
@Builder
public class RoomJoinResponse {
    @JsonProperty("room_member_idx")
    private Long roomMemberIdx;

    @JsonProperty("room_idx")
    private Long roomIdx;

    @JsonProperty("user_idx")
    private Long userIdx;

    private String role;

    @JsonProperty("joined_at")
    private LocalDateTime joinedAt;

    @JsonProperty("current_participant")
    private int currentParticipant;

    @JsonProperty("max_participant")
    private int maxParticipant;
}
