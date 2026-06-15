package chainsawman.gesture.dto.chatRoom.response;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Builder;
import lombok.Getter;

import java.util.List;

@Getter
@Builder
public class ChatMessageListResponse {

    private List<ChatMessageResponse> messages;

    @JsonProperty("next_cursor")
    private Long nextCursor;

    @JsonProperty("has_next")
    private boolean hasNext;
}
