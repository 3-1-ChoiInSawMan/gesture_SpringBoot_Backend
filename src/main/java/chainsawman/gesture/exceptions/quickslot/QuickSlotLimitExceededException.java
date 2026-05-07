package chainsawman.gesture.exceptions.quickslot;

import chainsawman.gesture.exceptions.common.DomainException;
import org.springframework.http.HttpStatus;

public class QuickSlotLimitExceededException extends DomainException {
    public QuickSlotLimitExceededException() {
        super(HttpStatus.BAD_REQUEST, "QUICK_SLOT_001", "퀵슬롯은 최대 30개까지 등록할 수 있습니다.");
    }
}
