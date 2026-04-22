package chainsawman.gesture.dto.room.response;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Builder;
import lombok.Getter;

import java.time.LocalDateTime;

@Getter
@Builder
public class RoomPatchResponse {
    @JsonProperty("room_id")
    private Long roomId;

    private String title;
    private String category;

    @JsonProperty("max_participant")
    private int maxParticipant;

    @JsonProperty("is_public")
    private boolean publicRoom;

    @JsonProperty("has_password")
    private boolean hasPassword;

    @JsonProperty("updated_at")
    private LocalDateTime updatedAt;
}
