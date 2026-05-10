package chainsawman.gesture.dto.meeting.request;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.util.List;

@Getter
@NoArgsConstructor
public class MeetingMinutesCreateRequest {

    private String title;

    private List<String> transcript;

    private List<String> participants;

    @JsonProperty("ai_summary")
    private String aiSummary;

    private List<String> conclusion;
}
