package chainsawman.gesture.dto.user.response;

import chainsawman.gesture.entity.user.User;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Builder;
import lombok.Getter;

import java.time.LocalDateTime;

@Getter
@Builder
public class PatchMyProfileResponse {
    private Long idx;
    private String nickname;
    @JsonProperty("profile_url")
    private String profileUrl;
    @JsonProperty("status_message")
    private String statusMessage;
    @JsonProperty("updated_at")
    private LocalDateTime updatedAt;

    public static PatchMyProfileResponse from(User user, String profileUrl) {
        return PatchMyProfileResponse.builder()
                .idx(user.getIdx())
                .nickname(user.getNickname())
                .profileUrl(profileUrl)
                .statusMessage(user.getStatusMessage())
                .updatedAt(user.getUpdatedAt())
                .build();
    }
}
