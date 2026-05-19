package chainsawman.gesture.dto.user.request;

import com.fasterxml.jackson.annotation.JsonProperty;
import jakarta.validation.constraints.NotBlank;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor
@AllArgsConstructor
public class PatchPasswordRequest {
    @NotBlank
    @JsonProperty("current_password")
    private String currentPassword;
    @NotBlank
    @JsonProperty("new_password")
    private String newPassword;
}
