package chainsawman.gesture.dto.user.response;

import chainsawman.gesture.entity.user.User;
import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
public class UserResponse {
    private Long idx;
    private String id;
    private String nickname;
    private String profileUrl;
    private String statusMessage;
    private String provider;
    private Boolean isDeactivated;

    public static UserResponse from(User user, String profileUrl) {
        return UserResponse.builder()
                .idx(user.getIdx())
                .id(user.getId())
                .nickname(user.getNickname())
                .profileUrl(profileUrl)
                .statusMessage(user.getStatusMessage())
                .provider(user.getProvider() != null ? user.getProvider().name() : null)
                .isDeactivated(user.getIsDeactivated())
                .build();
    }
}
