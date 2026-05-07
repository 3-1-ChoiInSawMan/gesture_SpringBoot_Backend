package chainsawman.gesture.dto.quickSlot.response;

import chainsawman.gesture.entity.quick.QuickSlot;
import chainsawman.gesture.entity.user.User;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Builder;
import lombok.Getter;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Getter
@Builder
public class UpdateQuickSlotResponse {

    @JsonProperty("user_idx")
    private Long userIdx;

    @JsonProperty("quick_slots")
    private List<QuickSlotItem> quickSlots;

    @JsonProperty("updated_at")
    private LocalDateTime updatedAt;

    @Getter
    @Builder
    public static class QuickSlotItem {
        @JsonProperty("quick_slot_id")
        private Long quickSlotId;
        private String name;
        private int order;
    }

    public static UpdateQuickSlotResponse of(User user, List<QuickSlot> orderedSlots, LocalDateTime updatedAt) {
        List<QuickSlotItem> items = new ArrayList<>();
        for (int i = 0; i < orderedSlots.size(); i++) {
            QuickSlot slot = orderedSlots.get(i);
            items.add(QuickSlotItem.builder()
                    .quickSlotId(slot.getIdx())
                    .name(slot.getName())
                    .order(i + 1)
                    .build());
        }
        return UpdateQuickSlotResponse.builder()
                .userIdx(user.getIdx())
                .quickSlots(items)
                .updatedAt(updatedAt)
                .build();
    }
}
