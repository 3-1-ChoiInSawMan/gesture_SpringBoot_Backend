package chainsawman.gesture.dto.quickSlot.response;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Builder;
import lombok.Getter;

import java.time.LocalDateTime;

@Getter
@Builder
public class DeleteQuickSlotResponse {

    @JsonProperty("quick_slot_idx")
    private Long quickSlotIdx;

    private boolean deleted;

    @JsonProperty("deleted_at")
    private LocalDateTime deletedAt;
}
