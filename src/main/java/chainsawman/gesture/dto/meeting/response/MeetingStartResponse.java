package chainsawman.gesture.dto.meeting.response;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Builder;
import lombok.Getter;

import java.time.LocalDateTime;

@Getter
@Builder
public class MeetingStartResponse {

    @JsonProperty("minutes_idx")
    private Long minutesIdx;

    @JsonProperty("call_idx")
    private Long callIdx;

    @JsonProperty("room_idx")
    private Long roomIdx;

    @JsonProperty("started_at")
    private LocalDateTime startedAt;

    private String status;
}
