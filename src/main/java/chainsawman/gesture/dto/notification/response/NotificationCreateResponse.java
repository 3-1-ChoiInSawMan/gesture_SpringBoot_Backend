package chainsawman.gesture.dto.notification.response;

import chainsawman.gesture.entity.notification.Notification;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Builder;
import lombok.Getter;

import java.time.LocalDateTime;

@Getter
@Builder
public class NotificationCreateResponse {

    @JsonProperty("notification_id")
    private Long notificationId;

    @JsonProperty("receiver_id")
    private Long receiverId;

    private String type;

    @JsonProperty("is_read")
    private boolean isRead;

    @JsonProperty("created_at")
    private LocalDateTime createdAt;

    public static NotificationCreateResponse from(Notification notification) {
        return NotificationCreateResponse.builder()
                .notificationId(notification.getIdx())
                .receiverId(notification.getUser().getIdx())
                .type(notification.getType().name())
                .isRead(notification.isRead())
                .createdAt(notification.getCreatedAt())
                .build();
    }
}
