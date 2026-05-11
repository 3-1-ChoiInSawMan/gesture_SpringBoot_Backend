package chainsawman.gesture.dto.call.response;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Builder;
import lombok.Getter;

import java.time.LocalDateTime;

@Getter
@Builder
public class CallLeaveResponse {

    @JsonProperty("call_idx")
    private Long callIdx;

    @JsonProperty("room_idx")
    private Long roomIdx;

    @JsonProperty("user_idx")
    private Long userIdx;

    @JsonProperty("left_at")
    private LocalDateTime leftAt;

    @JsonProperty("current_participant")
    private int currentParticipant;

    @JsonProperty("call_ended")
    private boolean callEnded;
}
