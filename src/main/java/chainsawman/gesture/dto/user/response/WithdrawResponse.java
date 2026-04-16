package chainsawman.gesture.dto.user.response;

import chainsawman.gesture.entity.user.User;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
public class WithdrawResponse {
    private Long idx;
    private String id;
    private String nickname;
    @JsonProperty("is_deactivated")
    private Boolean isDeactivated;

    public static WithdrawResponse from(User user) {
        return WithdrawResponse.builder()
                .idx(user.getIdx())
                .id(user.getId())
                .nickname(user.getNickname())
                .isDeactivated(user.getIsDeactivated())
                .build();
    }
}
