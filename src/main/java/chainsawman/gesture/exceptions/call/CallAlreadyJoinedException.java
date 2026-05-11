package chainsawman.gesture.exceptions.call;

import chainsawman.gesture.exceptions.common.DomainException;
import org.springframework.http.HttpStatus;

public class CallAlreadyJoinedException extends DomainException {
    public CallAlreadyJoinedException() {
        super(HttpStatus.CONFLICT, "CALL_004", "이미 참여 중인 통화입니다.");
    }
}
