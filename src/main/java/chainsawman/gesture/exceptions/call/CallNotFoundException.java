package chainsawman.gesture.exceptions.call;

import chainsawman.gesture.exceptions.common.DomainException;
import org.springframework.http.HttpStatus;

public class CallNotFoundException extends DomainException {
    public CallNotFoundException() {
        super(HttpStatus.NOT_FOUND, "CALL_001", "통화를 찾을 수 없습니다.");
    }
}