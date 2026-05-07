package chainsawman.gesture.dto.quickSlot.request;

import com.fasterxml.jackson.annotation.JsonProperty;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.util.List;

@Getter
@NoArgsConstructor
@AllArgsConstructor
public class UpdateQuickSlotRequest {

    @NotNull(message = "퀵슬롯 ID 목록은 필수입니다.")
    @Size(max = 5, message = "퀵슬롯은 최대 5개까지 등록할 수 있습니다.")
    @JsonProperty("quick_slot_ids")
    private List<Long> quickSlotIds;
}
