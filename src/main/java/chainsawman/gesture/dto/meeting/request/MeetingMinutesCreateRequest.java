package chainsawman.gesture.dto.meeting.request;

import com.fasterxml.jackson.annotation.JsonProperty;
import jakarta.validation.constraints.NotBlank;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.util.List;

@Getter
@NoArgsConstructor
@Builder
@AllArgsConstructor
public class MeetingMinutesCreateRequest {

    @NotBlank
    private String title;

    private List<String> transcript;

    private List<String> participants;

    @JsonProperty("ai_summary")
    private String aiSummary;

    private List<String> conclusion;
}
