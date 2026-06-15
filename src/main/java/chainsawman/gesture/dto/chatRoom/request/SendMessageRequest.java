package chainsawman.gesture.dto.chatRoom.request;

import chainsawman.gesture.enums.MessageType;
import com.fasterxml.jackson.annotation.JsonProperty;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor
@AllArgsConstructor
public class SendMessageRequest {

    @NotNull
    private MessageType type;

    private String message;

    @JsonProperty("file_uuid")
    private String fileUuid;
}
