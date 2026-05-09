package chainsawman.gesture.dto.room.response;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
public class RoomLeaveResponse {

    @JsonProperty("room_idx")
    private Long roomIdx;

    private boolean deleted;

    @JsonProperty("new_host_idx")
    private Long newHostIdx;
}
