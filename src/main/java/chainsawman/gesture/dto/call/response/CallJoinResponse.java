package chainsawman.gesture.dto.call.response;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Builder;
import lombok.Getter;

import java.time.LocalDateTime;

@Getter
@Builder
public class CallJoinResponse {

    @JsonProperty("call_idx")
    private Long callIdx;

    @JsonProperty("room_idx")
    private Long roomIdx;

    @JsonProperty("user_idx")
    private Long userIdx;

    @JsonProperty("joined_at")
    private LocalDateTime joinedAt;

    @JsonProperty("current_participant")
    private int currentParticipant;

    @JsonProperty("max_participant")
    private int maxParticipant;
}
