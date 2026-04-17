package chainsawman.gesture.dto.user.response;

import chainsawman.gesture.entity.user.User;
import lombok.AllArgsConstructor;
import lombok.Getter;


@Getter
@AllArgsConstructor
public class RefreshTokenValidationResponse {
    private Long idx;

    public static RefreshTokenValidationResponse from(User user) {
        return new RefreshTokenValidationResponse(user.getIdx());
    }
}
