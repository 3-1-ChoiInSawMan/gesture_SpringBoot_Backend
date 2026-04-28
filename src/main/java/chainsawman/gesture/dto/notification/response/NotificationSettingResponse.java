package chainsawman.gesture.dto.notification.response;

import chainsawman.gesture.entity.notification.NotificationSetting;
import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
public class NotificationSettingResponse {

    private String type;
    private boolean enabled;

    public static NotificationSettingResponse from(NotificationSetting setting) {
        return NotificationSettingResponse.builder()
                .type(setting.getType().name())
                .enabled(setting.isUsed())
                .build();
    }
}
