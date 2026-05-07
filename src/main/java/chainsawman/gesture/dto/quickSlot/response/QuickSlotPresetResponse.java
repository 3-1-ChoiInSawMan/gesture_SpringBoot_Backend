package chainsawman.gesture.dto.quickSlot.response;

import chainsawman.gesture.entity.quick.QuickSlot;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Builder;
import lombok.Getter;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;
import java.util.stream.IntStream;

@Getter
@Builder
public class QuickSlotPresetResponse {

    @JsonProperty("user_idx")
    private Long userIdx;

    @JsonProperty("quick_slots")
    private List<PresetSlotItem> quickSlots;

    @JsonProperty("updated_at")
    private LocalDateTime updatedAt;

    @Getter
    @Builder
    public static class PresetSlotItem {
        @JsonProperty("quick_slot_id")
        private Long quickSlotId;
        private String name;
        @JsonProperty("icon_uuid")
        private String iconUuid;
        @JsonProperty("icon_url")
        private String iconUrl;
        private int order;
    }

    public static QuickSlotPresetResponse of(Long userIdx, List<QuickSlot> activeSlots,
                                             Map<String, String> urlMap, LocalDateTime updatedAt) {
        List<PresetSlotItem> items = IntStream.range(0, activeSlots.size())
                .mapToObj(i -> {
                    QuickSlot slot = activeSlots.get(i);
                    return PresetSlotItem.builder()
                            .quickSlotId(slot.getIdx())
                            .name(slot.getName())
                            .iconUuid(slot.getIconUuid())
                            .iconUrl(urlMap.getOrDefault(slot.getIconUuid(), null))
                            .order(i + 1)
                            .build();
                })
                .collect(java.util.stream.Collectors.toList());

        return QuickSlotPresetResponse.builder()
                .userIdx(userIdx)
                .quickSlots(items)
                .updatedAt(updatedAt)
                .build();
    }
}
