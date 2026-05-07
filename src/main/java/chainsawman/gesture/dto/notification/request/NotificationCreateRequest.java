package chainsawman.gesture.dto.notification.request;

import com.fasterxml.jackson.annotation.JsonProperty;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor
@AllArgsConstructor
public class NotificationCreateRequest {

    @NotNull
    @JsonProperty("receiver_id")
    private Long receiverId;

    @NotBlank
    private String type;

    @JsonProperty("actor_id")
    private Long actorId;

    @JsonProperty("target_id")
    private Long targetId;
}
