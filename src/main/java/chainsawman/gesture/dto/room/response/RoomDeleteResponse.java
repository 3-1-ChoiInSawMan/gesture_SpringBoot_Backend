package chainsawman.gesture.dto.room.response;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Builder;
import lombok.Getter;

import java.time.LocalDateTime;

@Getter
@Builder
public class RoomDeleteResponse {
    private boolean deleted;

    @JsonProperty("room_id")
    private Long roomId;

    @JsonProperty("deleted_at")
    private LocalDateTime deletedAt;
}
