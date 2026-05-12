package chainsawman.gesture.dto.meeting.response;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Builder;
import lombok.Getter;

import java.time.LocalDateTime;

@Getter
@Builder
public class MeetingListItemResponse {

    @JsonProperty("minutes_idx")
    private Long minutesIdx;

    @JsonProperty("call_idx")
    private Long callIdx;

    private String title;

    @JsonProperty("meeting_date")
    private LocalDateTime meetingDate;

    private String status;
}
