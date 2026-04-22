package chainsawman.gesture.dto.room.response;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Builder;
import lombok.Getter;

import java.time.LocalDateTime;

@Getter
@Builder
public class RoomJoinResponse {
    @JsonProperty("room_id")
    private Long roomId;

    @JsonProperty("user_idx")
    private Long userIdx;

    @JsonProperty("joined_at")
    private LocalDateTime joinedAt;

    @JsonProperty("current_participant")
    private int currentParticipant;

    @JsonProperty("max_participant")
    private int maxParticipant;
}
