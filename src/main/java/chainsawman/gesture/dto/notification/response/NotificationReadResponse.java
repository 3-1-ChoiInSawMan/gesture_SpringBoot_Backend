package chainsawman.gesture.dto.notification.response;

import chainsawman.gesture.entity.notification.Notification;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Builder;
import lombok.Getter;

import java.time.LocalDateTime;

@Getter
@Builder
public class NotificationReadResponse {

    @JsonProperty("notification_id")
    private Long notificationId;

    @JsonProperty("is_read")
    private boolean isRead;

    @JsonProperty("updated_at")
    private LocalDateTime updatedAt;

    public static NotificationReadResponse from(Notification notification) {
        return NotificationReadResponse.builder()
                .notificationId(notification.getIdx())
                .isRead(notification.isRead())
                .updatedAt(notification.getUpdatedAt())
                .build();
    }
}
