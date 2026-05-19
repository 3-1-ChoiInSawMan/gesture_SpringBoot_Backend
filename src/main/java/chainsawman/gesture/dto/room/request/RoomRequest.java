package chainsawman.gesture.dto.room.request;

import com.fasterxml.jackson.annotation.JsonProperty;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor
@AllArgsConstructor
public class RoomRequest {
    @NotBlank
    private String title;
    private String category;

    @Min(1)
    @JsonProperty("max_participant")
    private int maxParticipant;

    @JsonProperty("is_public")
    private boolean publicRoom;

    private String password;

    @JsonProperty("thumbnail_uuid")
    private String thumbnailUuid;
}
