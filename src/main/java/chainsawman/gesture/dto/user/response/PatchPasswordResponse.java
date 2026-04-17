package chainsawman.gesture.dto.user.response;

import chainsawman.gesture.entity.user.User;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.Getter;

import java.time.LocalDateTime;

@Getter
@AllArgsConstructor
public class PatchPasswordResponse {
    private Long idx;
    @JsonProperty("updated_at")
    private LocalDateTime updatedAt;

    public static PatchPasswordResponse from(User user) {
        return new PatchPasswordResponse(user.getIdx(), user.getUpdatedAt());
    }
}
