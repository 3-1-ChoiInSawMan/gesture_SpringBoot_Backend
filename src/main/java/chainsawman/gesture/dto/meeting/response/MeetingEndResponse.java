package chainsawman.gesture.dto.meeting.response;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Builder;
import lombok.Getter;

import java.time.LocalDateTime;
import java.util.List;

@Getter
@Builder
public class MeetingEndResponse {

    @JsonProperty("minutes_idx")
    private Long minutesIdx;

    @JsonProperty("call_idx")
    private Long callIdx;

    @JsonProperty("room_idx")
    private Long roomIdx;

    private String title;

    @JsonProperty("meeting_date")
    private LocalDateTime meetingDate;

    private List<String> participants;

    @JsonProperty("ai_summary")
    private String aiSummary;

    private List<String> conclusion;

    @JsonProperty("ended_at")
    private LocalDateTime endedAt;
}
