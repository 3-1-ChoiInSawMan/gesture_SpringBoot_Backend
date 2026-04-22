package chainsawman.gesture.dto.room.response;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
public class RoomListResponse {
    @JsonProperty("room_id")
    private Long roomId;

    private String title;
    private String category;

    @JsonProperty("current_participant")
    private int currentParticipant;

    @JsonProperty("max_participant")
    private int maxParticipant;

    @JsonProperty("is_public")
    private boolean publicRoom;

    @JsonProperty("has_password")
    private boolean hasPassword;

    @JsonProperty("thumbnail_url")
    private String thumbnailUrl;

    @JsonProperty("host_user_idx")
    private Long hostUserIdx;
}
