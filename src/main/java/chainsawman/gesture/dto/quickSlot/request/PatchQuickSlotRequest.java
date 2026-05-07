package chainsawman.gesture.dto.quickSlot.request;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor
@AllArgsConstructor
public class PatchQuickSlotRequest {

    private String name;

    private String description;

    @JsonProperty("icon_uuid")
    private String iconUuid;
}
