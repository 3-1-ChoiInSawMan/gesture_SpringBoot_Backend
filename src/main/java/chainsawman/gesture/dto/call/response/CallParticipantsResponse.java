package chainsawman.gesture.dto.call.response;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Builder;
import lombok.Getter;

import java.util.List;

@Getter
@Builder
public class CallParticipantsResponse {

    @JsonProperty("call_idx")
    private Long callIdx;

    @JsonProperty("room_idx")
    private Long roomIdx;

    private List<CallParticipantInfo> participants;

    @JsonProperty("current_participant")
    private int currentParticipant;
}
