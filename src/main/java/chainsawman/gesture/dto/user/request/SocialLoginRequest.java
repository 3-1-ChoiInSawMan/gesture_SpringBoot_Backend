package chainsawman.gesture.dto.user.request;

import com.fasterxml.jackson.annotation.JsonProperty;
import jakarta.validation.constraints.NotBlank;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor
@AllArgsConstructor
public class SocialLoginRequest {
    @NotBlank(message = "provider 값을 입력해주세요.")
    private String provider;

    @NotBlank(message = "provider_id 값을 입력해주세요.")
    @JsonProperty("provider_id")
    private String providerId;

    @NotBlank(message = "이메일을 입력해주세요.")
    private String email;
}
