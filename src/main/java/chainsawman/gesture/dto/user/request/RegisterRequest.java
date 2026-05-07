package chainsawman.gesture.dto.user.request;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor
@AllArgsConstructor
public class RegisterRequest {
    private String id;
    private String email;
    private String password;
    private String nickname;
    @JsonProperty("profile_image_uuid")
    private String profileImageUuid;
}
