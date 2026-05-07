package chainsawman.gesture.dto.quickSlot.request;

import com.fasterxml.jackson.annotation.JsonProperty;
import jakarta.validation.constraints.NotBlank;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor
@AllArgsConstructor
public class QuickSlotRequest {

    @NotBlank(message = "슬롯이름은 필수 입니다.")
    private String name;
    private String description;

    @NotBlank(message = "슬롯 영상은 필수 입니다.")
    @JsonProperty("icon_uuid")
    private String iconUuid;
}
