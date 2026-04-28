package chainsawman.gesture.dto.notification.response;

import chainsawman.gesture.entity.notification.NotificationSetting;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Builder;
import lombok.Getter;

import java.time.LocalDateTime;

@Getter
@Builder
public class NotificationSettingPatchResponse {

    @JsonProperty("notification_setting_id")
    private Long notificationSettingId;

    private String type;

    private boolean enabled;

    @JsonProperty("updated_at")
    private LocalDateTime updatedAt;

    public static NotificationSettingPatchResponse from(NotificationSetting setting) {
        return NotificationSettingPatchResponse.builder()
                .notificationSettingId(setting.getIdx())
                .type(setting.getType().name())
                .enabled(setting.isUsed())
                .updatedAt(setting.getUpdatedAt())
                .build();
    }
}
