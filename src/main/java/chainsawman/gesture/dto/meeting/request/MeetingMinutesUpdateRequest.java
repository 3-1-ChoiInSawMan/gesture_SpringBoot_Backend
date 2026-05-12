package chainsawman.gesture.dto.meeting.request;

import lombok.Getter;
import lombok.NoArgsConstructor;

import java.util.List;

@Getter
@NoArgsConstructor
public class MeetingMinutesUpdateRequest {

    private String title;
    private String content;
    private List<String> conclusion;
}
