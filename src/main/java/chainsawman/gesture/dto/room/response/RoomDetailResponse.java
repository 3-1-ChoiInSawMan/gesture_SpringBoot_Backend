package chainsawman.gesture.dto.room.response;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
public class RoomDetailResponse {
    @JsonProperty("room_idx")
    private Long roomIdx;

    private String title;
    private String category;

    @JsonProperty("max_participant")
    private int maxParticipant;

    @JsonProperty("current_participant")
    private int currentParticipant;

    @JsonProperty("is_public")
    private boolean publicRoom;

    @JsonProperty("has_password")
    private boolean hasPassword;

    @JsonProperty("thumbnail_url")
    private String thumbnailUrl;

    @JsonProperty("host_user_idx")
    private Long hostUserIdx;
}
