package chainsawman.gesture.dto.chatRoom.response;

import chainsawman.gesture.entity.chat.ChatParticipant;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
public class ReadMarkResponse {

    @JsonProperty("chat_room_idx")
    private Long chatRoomIdx;

    @JsonProperty("last_read_message_idx")
    private Long lastReadMessageIdx;

    public static ReadMarkResponse from(ChatParticipant participant) {
        return ReadMarkResponse.builder()
                .chatRoomIdx(participant.getChatRoom().getIdx())
                .lastReadMessageIdx(participant.getLastReadMessageIdx())
                .build();
    }
}
