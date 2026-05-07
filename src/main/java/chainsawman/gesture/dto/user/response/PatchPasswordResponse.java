package chainsawman.gesture.dto.user.response;

import chainsawman.gesture.entity.user.User;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Builder;
import lombok.Getter;

import java.time.LocalDateTime;

@Getter
@Builder
public class PatchPasswordResponse {
    private Long idx;
    @JsonProperty("updated_at")
    private LocalDateTime updatedAt;

    public static PatchPasswordResponse from(User user) {
        return PatchPasswordResponse.builder()
                .idx(user.getIdx())
                .updatedAt(user.getUpdatedAt())
                .build();
    }
}
