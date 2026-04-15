package chainsawman.gesture.dto.user.response;

import chainsawman.gesture.entity.user.User;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Builder;
import lombok.Getter;

import java.time.LocalDateTime;

@Getter
@Builder
public class LoginResponse {

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

    @JsonProperty("access_token")
    private String accessToken;

    @JsonProperty("refresh_token")
    private String refreshToken;

    @JsonProperty("created_at")
    private LocalDateTime createdAt;

    @JsonProperty("updated_at")
    private LocalDateTime updatedAt;


    public static LoginResponse from(User user, String profileUrl, String accessToken, String refreshToken) {
        return LoginResponse.builder()
                .idx(user.getIdx())
                .id(user.getId())
                .email(user.getEmail())
                .nickname(user.getNickname())
                .profileUrl(profileUrl)
                .statusMessage(user.getStatusMessage())
                .provider(user.getProvider() != null ? user.getProvider().name() : null)
                .isDeactivated(user.getIsDeactivated())
                .accessToken(accessToken)
                .refreshToken(refreshToken)
                .createdAt(user.getCreatedAt())
                .updatedAt(user.getUpdatedAt())
                .build();
    }
}

