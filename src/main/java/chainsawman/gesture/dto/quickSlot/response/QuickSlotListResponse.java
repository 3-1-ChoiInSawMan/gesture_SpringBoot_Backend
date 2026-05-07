package chainsawman.gesture.dto.quickSlot.response;

import chainsawman.gesture.entity.quick.QuickSlot;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Builder;
import lombok.Getter;

import java.time.LocalDateTime;

@Getter
@Builder
public class QuickSlotListResponse {

    private Long idx;

    private String name;

    private String description;

    @JsonProperty("icon_uuid")
    private String iconUuid;

    @JsonProperty("icon_url")
    private String iconUrl;

    private Integer order;

    @JsonProperty("created_at")
    private LocalDateTime createdAt;

    public static QuickSlotListResponse from(QuickSlot slot, String iconUrl) {
        return QuickSlotListResponse.builder()
                .idx(slot.getIdx())
                .name(slot.getName())
                .description(slot.getDescription())
                .iconUuid(slot.getIconUuid())
                .iconUrl(iconUrl)
                .order(slot.getOrder())
                .createdAt(slot.getCreatedAt())
                .build();
    }
}
