package chainsawman.gesture.dto.room.response;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Builder;
import lombok.Getter;

import java.time.LocalDateTime;

@Getter
@Builder
public class RoomResponse {
    @JsonProperty("room_idx")
    private Long roomIdx;

    @JsonProperty("chat_room_idx")
    private Long chatRoomIdx;

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

    @JsonProperty("created_at")
    private LocalDateTime createdAt;
}
