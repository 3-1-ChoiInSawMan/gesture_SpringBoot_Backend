package chainsawman.gesture.dto.user.response;

import chainsawman.gesture.entity.user.User;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Builder;
import lombok.Getter;

import java.time.LocalDateTime;

@Getter
@Builder
public class ProfileResponse {
    private Long idx;
    private String id;
    private String nickname;
    @JsonProperty("profile_url")
    private String profileUrl;
    @JsonProperty("status_message")
    private String statusMessage;
    @JsonProperty("is_deactivated")
    private Boolean isDeactivated;
    @JsonProperty("created_at")
    private LocalDateTime createdAt;

    public static ProfileResponse from(User user, String profileUrl) {
        return ProfileResponse.builder()
                .idx(user.getIdx())
                .id(user.getId())
                .nickname(user.getNickname())
                .profileUrl(profileUrl)
                .statusMessage(user.getStatusMessage())
                .isDeactivated(user.getIsDeactivated())
                .createdAt(user.getCreatedAt())
                .build();
    }
}
