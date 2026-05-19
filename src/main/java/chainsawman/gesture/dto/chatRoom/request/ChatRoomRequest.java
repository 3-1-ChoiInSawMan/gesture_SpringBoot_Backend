package chainsawman.gesture.dto.chatRoom.request;

import com.fasterxml.jackson.annotation.JsonProperty;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import java.util.List;

@Getter
@NoArgsConstructor
@AllArgsConstructor
public class ChatRoomRequest {

    @NotBlank
    private String name;

    @JsonProperty("image_uuid")
    private String imageUuid;

    @NotNull
    @JsonProperty("participant_ids")
    private List<Long> participantIds;

}
