package chainsawman.gesture.dto.room.response;

import chainsawman.gesture.entity.user.User;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
public class HostInfo {
    @JsonProperty("user_idx")
    private Long userIdx;

    @JsonProperty("user_name")
    private String userName;

    @JsonProperty("profile_url")
    private String profileUrl;

    public static HostInfo from(User host, String profileUrl) {
        return HostInfo.builder()
                .userIdx(host.getIdx())
                .userName(host.getNickname())
                .profileUrl(profileUrl)
                .build();
    }
}
