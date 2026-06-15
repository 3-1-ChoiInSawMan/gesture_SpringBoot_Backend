package chainsawman.gesture.dto.chatRoom.response;

import chainsawman.gesture.entity.chat.ChatParticipant;
import chainsawman.gesture.entity.chat.ChatRoom;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Builder;
import lombok.Getter;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;

@Getter
@Builder
public class ChatRoomDetailResponse {

    @JsonProperty("chat_room_idx")
    private Long chatRoomIdx;

    private String name;

    @JsonProperty("image_url")
    private String imageUrl;

    private List<ParticipantInfo> participants;

    @JsonProperty("created_at")
    private LocalDateTime createdAt;

    @JsonProperty("updated_at")
    private LocalDateTime updatedAt;

    public static ChatRoomDetailResponse from(ChatRoom chatRoom, List<ChatParticipant> participants,
                                              String imageUrl, Map<Long, String> profileImageUrlMap) {
        return ChatRoomDetailResponse.builder()
                .chatRoomIdx(chatRoom.getIdx())
                .name(chatRoom.getName())
                .imageUrl(imageUrl)
                .participants(
                        participants.stream()
                                .map(cp -> ParticipantInfo.from(cp, profileImageUrlMap.get(cp.getUser().getIdx())))
                                .toList()
                )
                .createdAt(chatRoom.getCreatedAt())
                .updatedAt(chatRoom.getUpdatedAt())
                .build();
    }

    @Getter
    @Builder
    public static class ParticipantInfo {

        @JsonProperty("user_idx")
        private Long userIdx;

        private String nickname;

        @JsonProperty("user_id")
        private String userId;

        @JsonProperty("profile_image_url")
        private String profileImageUrl;

        @JsonProperty("last_read_message_idx")
        private Long lastReadMessageIdx;

        public static ParticipantInfo from(ChatParticipant cp, String profileImageUrl) {
            return ParticipantInfo.builder()
                    .userIdx(cp.getUser().getIdx())
                    .nickname(cp.getUser().getNickname())
                    .userId(cp.getUser().getId())
                    .profileImageUrl(profileImageUrl)
                    .lastReadMessageIdx(cp.getLastReadMessageIdx())
                    .build();
        }
    }
}
