package chainsawman.gesture.dto.notification.request;

import jakarta.validation.constraints.NotNull;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor
public class NotificationSettingPatchRequest {

    @NotNull
    private Boolean enabled;
}
