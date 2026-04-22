package chainsawman.gesture.dto.room.response;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Builder;
import lombok.Getter;

import java.time.LocalDateTime;

@Getter
@Builder
public class RoomDeleteResponse {
    private boolean deleted;

    @JsonProperty("room_idx")
    private Long roomIdx;

    @JsonProperty("deleted_at")
    private LocalDateTime deletedAt;
}
