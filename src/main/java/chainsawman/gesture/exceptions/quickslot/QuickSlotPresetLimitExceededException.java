package chainsawman.gesture.exceptions.quickslot;

import chainsawman.gesture.exceptions.common.DomainException;
import org.springframework.http.HttpStatus;

public class QuickSlotPresetLimitExceededException extends DomainException {
    public QuickSlotPresetLimitExceededException() {
        super(HttpStatus.BAD_REQUEST, "QUICK_SLOT_003", "프리셋에는 최대 5개의 퀵슬롯만 설정할 수 있습니다.");
    }
}
