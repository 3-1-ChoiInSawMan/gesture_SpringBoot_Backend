package chainsawman.gesture.dto.user.request;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public class PatchMyProfileRequest {
    private String nickname;
    @JsonProperty("profile_url")
    private String profileUrl;
    @JsonProperty("status_message")
    private String statusMessage;

}
