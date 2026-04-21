package chainsawman.gesture.dto.user.response;

import chainsawman.gesture.entity.user.User;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
@JsonInclude(JsonInclude.Include.NON_NULL)
public class SocialLoginResponse {

    @JsonProperty("is_new_user")
    private Boolean isNewUser;

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

    // 기존 유저 로그인 성공
    public static SocialLoginResponse existingUser(
            User user,
            String profileUrl,
            String accessToken,
            String refreshToken
    ) {
        return SocialLoginResponse.builder()
                .isNewUser(false)
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
                .build();
    }

    // 신규 유저 - 추가정보 입력 필요
    public static SocialLoginResponse newUser() {
        return SocialLoginResponse.builder()
                .isNewUser(true)
                .build();
    }
}