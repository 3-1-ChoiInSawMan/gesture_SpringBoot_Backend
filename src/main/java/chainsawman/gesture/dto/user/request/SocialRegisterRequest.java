package chainsawman.gesture.dto.user.request;

import com.fasterxml.jackson.annotation.JsonProperty;
import jakarta.validation.constraints.NotBlank;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor
public class SocialRegisterRequest {

    @NotBlank(message = "provider 값을 입력해주세요.")
    @JsonProperty("provider")
    private String provider;

    @NotBlank(message = "provider_id 값을 입력해주세요.")
    @JsonProperty("provider_id")
    private String providerId;

    @NotBlank(message = "이메일을 입력해주세요.")
    @JsonProperty("email")
    private String email;

    @NotBlank(message = "아이디를 입력해주세요.")
    @JsonProperty("id")
    private String id;

    @NotBlank(message = "닉네임을 입력해주세요.")
    @JsonProperty("nickname")
    private String nickname;

    @JsonProperty("profile_image_uuid")
    private String profileImageUuid;
}
