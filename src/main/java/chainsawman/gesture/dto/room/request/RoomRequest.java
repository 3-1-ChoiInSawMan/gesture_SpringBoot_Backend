package chainsawman.gesture.dto.room.request;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor
@AllArgsConstructor
public class RoomRequest {
    private String title;
    private String category;

    @JsonProperty("max_participant")
    private int maxParticipant;

    @JsonProperty("is_public")
    private boolean publicRoom;

    private String password;

    @JsonProperty("thumbnail_uuid")
    private String thumbnailUuid;
}
