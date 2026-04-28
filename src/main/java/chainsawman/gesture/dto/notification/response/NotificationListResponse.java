package chainsawman.gesture.dto.notification.response;

import chainsawman.gesture.dto.notification.info.ActorInfo;
import chainsawman.gesture.entity.notification.Notification;
import chainsawman.gesture.entity.user.User;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Builder;
import lombok.Getter;

import java.time.LocalDateTime;

@Getter
@Builder
public class NotificationListResponse {

    private Long idx;

    private String type;

    @JsonProperty("is_read")
    private boolean isRead;

    private String content;

    private ActorInfo actor;

    @JsonProperty("target_id")
    private String targetId;

    @JsonProperty("created_at")
    private LocalDateTime createdAt;

    public static NotificationListResponse from(Notification notification) {
        return NotificationListResponse.builder()
                .idx(notification.getIdx())
                .type(notification.getType().name())
                .isRead(notification.isRead())
                .content(notification.getContent())
                .actor(ActorInfo.from(notification.getActor()))
                .targetId(notification.getTargetId())
                .createdAt(notification.getCreatedAt())
                .build();
    }


}
