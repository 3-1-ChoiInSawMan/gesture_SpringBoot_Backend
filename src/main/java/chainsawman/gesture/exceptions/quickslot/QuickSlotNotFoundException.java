package chainsawman.gesture.exceptions.quickslot;

import chainsawman.gesture.exceptions.common.DomainException;
import org.springframework.http.HttpStatus;

public class QuickSlotNotFoundException extends DomainException {
    public QuickSlotNotFoundException() {
        super(HttpStatus.NOT_FOUND, "QUICK_SLOT_002", "존재하지 않는 퀵슬롯입니다.");
    }
}
