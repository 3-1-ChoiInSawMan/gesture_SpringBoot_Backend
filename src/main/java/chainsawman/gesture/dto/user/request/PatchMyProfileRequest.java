package chainsawman.gesture.dto.user.request;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public class PatchMyProfileRequest {
    private String nickname;
    @JsonProperty("profile_image_uuid")
    private String profileImageUuid;
    @JsonProperty("status_message")
    private String statusMessage;
}
