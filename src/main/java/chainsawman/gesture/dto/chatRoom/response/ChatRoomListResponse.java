package chainsawman.gesture.dto.chatRoom.response;

import chainsawman.gesture.entity.chat.ChatRoom;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Builder;
import lombok.Getter;

import java.time.LocalDateTime;

@Getter
@Builder
public class ChatRoomListResponse {

    @JsonProperty("chat_room_idx")
    private Long chatRoomIdx;

    private String name;

    @JsonProperty("image_url")
    private String imageUrl;

    @JsonProperty("participant_count")
    private int participantCount;

    @JsonProperty("created_at")
    private LocalDateTime createdAt;

    public static ChatRoomListResponse from(ChatRoom chatRoom, int participantCount, String imageUrl) {
        return ChatRoomListResponse.builder()
                .chatRoomIdx(chatRoom.getIdx())
                .name(chatRoom.getName())
                .imageUrl(imageUrl)
                .participantCount(participantCount)
                .createdAt(chatRoom.getCreatedAt())
                .build();
    }
}
