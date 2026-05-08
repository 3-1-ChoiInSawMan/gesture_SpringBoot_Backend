package chainsawman.gesture.dto.chatRoom.request;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import java.util.List;

@Getter
@NoArgsConstructor
@AllArgsConstructor
public class ChatRoomRequest {

    private String name;

    @JsonProperty("image_uuid")
    private String imageUuid;

    @JsonProperty("participant_ids")
    private List<Long> participantIds;

}
