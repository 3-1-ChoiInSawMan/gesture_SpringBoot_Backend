package chainsawman.gesture.dto.user.response;

import chainsawman.gesture.entity.user.User;
import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
public class RefreshTokenValidationResponse {
    private Long idx;

    public static RefreshTokenValidationResponse from(User user) {
        return RefreshTokenValidationResponse.builder()
                .idx(user.getIdx())
                .build();
    }
}
