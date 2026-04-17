package chainsawman.gesture.dto.user.request;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public class RefreshTokenRequest {

    @JsonProperty("refresh_token")
    private String refreshToken;
}
