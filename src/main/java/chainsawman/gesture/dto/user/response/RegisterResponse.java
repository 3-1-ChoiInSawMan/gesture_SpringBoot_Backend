package chainsawman.gesture.dto.user.response;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Builder;
import lombok.Getter;

import java.time.LocalDateTime;

@Getter
@Builder
public class RegisterResponse {
    private Long idx;
    private String id;
    private String email;
    private String nickname;
    @JsonProperty("profile_url")
    private String profileUrl;
    @JsonProperty("status_message")
    private String statusMessage;
    private String provider;
    @JsonProperty("is_deactivated")
    private Boolean isDeactivated;
    @JsonProperty("created_at")
    private LocalDateTime createdAt;
}
