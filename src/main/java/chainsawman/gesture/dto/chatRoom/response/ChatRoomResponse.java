package chainsawman.gesture.dto.chatRoom.response;

import chainsawman.gesture.entity.chat.ChatRoom;
import chainsawman.gesture.entity.user.User;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Builder;
import lombok.Getter;

import java.time.LocalDateTime;
import java.util.List;

@Getter
@Builder
public class ChatRoomResponse {

    @JsonProperty("chat_room_idx")
    private Long chatRoomIdx;

    @JsonProperty("image_url")
    private String imageUrl;

    private List<Participant> participants;

    @JsonProperty("created_at")
    private LocalDateTime createdAt;

    public static ChatRoomResponse from(ChatRoom chatRoom, List<User> users, String imageUrl) {
        return ChatRoomResponse.builder()
                .chatRoomIdx(chatRoom.getIdx())
                .imageUrl(imageUrl)
                .participants(
                        users.stream()
                                .map(Participant::from)
                                .toList()
                )
                .createdAt(chatRoom.getCreatedAt())
                .build();
    }

    @Getter
    @Builder
    public static class Participant {

        @JsonProperty("user_idx")
        private Long userIdx;

        private String nickname;


        public static Participant from(User user) {
            return Participant.builder()
                    .userIdx(user.getIdx())
                    .nickname(user.getNickname())
                    .build();
        }
    }
}
