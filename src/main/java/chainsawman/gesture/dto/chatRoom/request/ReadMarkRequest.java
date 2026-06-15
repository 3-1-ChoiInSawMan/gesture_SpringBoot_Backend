package chainsawman.gesture.dto.chatRoom.request;

import com.fasterxml.jackson.annotation.JsonProperty;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor
@AllArgsConstructor
public class ReadMarkRequest {

    @NotNull
    @JsonProperty("last_read_message_idx")
    private Long lastReadMessageIdx;
}
