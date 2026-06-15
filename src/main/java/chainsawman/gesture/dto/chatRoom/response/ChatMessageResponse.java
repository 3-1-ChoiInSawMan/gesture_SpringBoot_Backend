package chainsawman.gesture.dto.chatRoom.response;

import chainsawman.gesture.entity.chat.ChatMessage;
import chainsawman.gesture.enums.MessageType;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Builder;
import lombok.Getter;

import java.time.LocalDateTime;

@Getter
@Builder
public class ChatMessageResponse {

    @JsonProperty("message_idx")
    private Long messageIdx;

    private SenderInfo sender;

    private String message;

    private MessageType type;

    @JsonProperty("is_deleted")
    private boolean isDeleted;

    @JsonProperty("file_url")
    private String fileUrl;

    @JsonProperty("created_at")
    private LocalDateTime createdAt;

    public static ChatMessageResponse from(ChatMessage chatMessage, String senderProfileImageUrl, String fileUrl) {
        return ChatMessageResponse.builder()
                .messageIdx(chatMessage.getIdx())
                .sender(SenderInfo.from(chatMessage, senderProfileImageUrl))
                .message(chatMessage.isDeleted() ? null : chatMessage.getMessage())
                .type(chatMessage.getType())
                .isDeleted(chatMessage.isDeleted())
                .fileUrl(chatMessage.isDeleted() ? null : fileUrl)
                .createdAt(chatMessage.getCreatedAt())
                .build();
    }

    @Getter
    @Builder
    public static class SenderInfo {

        @JsonProperty("user_idx")
        private Long userIdx;

        private String nickname;

        @JsonProperty("user_id")
        private String userId;

        @JsonProperty("profile_image_url")
        private String profileImageUrl;

        public static SenderInfo from(ChatMessage chatMessage, String profileImageUrl) {
            return SenderInfo.builder()
                    .userIdx(chatMessage.getSender().getIdx())
                    .nickname(chatMessage.getSender().getNickname())
                    .userId(chatMessage.getSender().getId())
                    .profileImageUrl(profileImageUrl)
                    .build();
        }
    }
}
