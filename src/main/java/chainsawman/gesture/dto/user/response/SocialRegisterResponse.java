package chainsawman.gesture.dto.user.response;

import chainsawman.gesture.entity.user.User;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
public class SocialRegisterResponse {

    @JsonProperty("idx")
    private Long idx;

    @JsonProperty("id")
    private String id;

    @JsonProperty("email")
    private String email;

    @JsonProperty("nickname")
    private String nickname;

    @JsonProperty("profile_url")
    private String profileUrl;

    @JsonProperty("status_message")
    private String statusMessage;

    @JsonProperty("provider")
    private String provider;

    @JsonProperty("is_deactivated")
    private Boolean isDeactivated;

    @JsonProperty("access_token")
    private String accessToken;

    @JsonProperty("refresh_token")
    private String refreshToken;

    public static SocialRegisterResponse from(
            User user,
            String accessToken,
            String refreshToken
    ) {
        return SocialRegisterResponse.builder()
                .idx(user.getIdx())
                .id(user.getId())
                .email(user.getEmail())
                .nickname(user.getNickname())
                .profileUrl(null)
                .statusMessage(null)
                .provider(user.getProvider() != null ? user.getProvider().name() : null)
                .isDeactivated(user.getIsDeactivated())
                .accessToken(accessToken)
                .refreshToken(refreshToken)
                .build();
    }
}
