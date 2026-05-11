package chainsawman.gesture.dto.call.response;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Builder;
import lombok.Getter;

import java.time.LocalDateTime;

@Getter
@Builder
public class CallParticipantInfo {

    @JsonProperty("user_idx")
    private Long userIdx;

    private String nickname;

    @JsonProperty("joined_at")
    private LocalDateTime joinedAt;

    @JsonProperty("is_host")
    private boolean isHost;
}
