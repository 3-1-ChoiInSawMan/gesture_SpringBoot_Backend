package chainsawman.gesture.dto.notification.info;

import chainsawman.gesture.entity.user.User;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
public class ActorInfo {

    private Long idx;

    private String nickname;

    @JsonProperty("user_id")
    private String userId;

    @JsonProperty("profile_image_url")
    private String profileImageUrl;

    public static ActorInfo from(User actor) {
        if (actor == null) {
            return null;
        }
        return ActorInfo.builder()
                .idx(actor.getIdx())
                .nickname(actor.getNickname())
                .userId(actor.getId())
                .profileImageUrl(null)
                .build();
    }
}
