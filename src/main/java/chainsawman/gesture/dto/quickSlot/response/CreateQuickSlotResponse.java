package chainsawman.gesture.dto.quickSlot.response;

import chainsawman.gesture.entity.quick.QuickSlot;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Builder;
import lombok.Getter;

import java.time.LocalDateTime;

@Getter
@Builder
public class CreateQuickSlotResponse {

    private Long idx;

    private String name;

    private String description;

    @JsonProperty("user_idx")
    private Long userIdx;

    @JsonProperty("icon_uuid")
    private String iconUuid;

    @JsonProperty("icon_url")
    private String iconUrl;

    @JsonProperty("created_at")
    private LocalDateTime createdAt;

    public static CreateQuickSlotResponse from(QuickSlot quickSlot, String iconUrl) {
        return CreateQuickSlotResponse.builder()
                .idx(quickSlot.getIdx())
                .name(quickSlot.getName())
                .description(quickSlot.getDescription())
                .userIdx(quickSlot.getUser().getIdx())
                .iconUuid(quickSlot.getIconUuid())
                .iconUrl(iconUrl)
                .createdAt(quickSlot.getCreatedAt())
                .build();
    }
}
