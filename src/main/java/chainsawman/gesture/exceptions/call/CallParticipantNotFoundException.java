package chainsawman.gesture.exceptions.call;

import chainsawman.gesture.exceptions.common.DomainException;
import org.springframework.http.HttpStatus;

public class CallParticipantNotFoundException extends DomainException {
    public CallParticipantNotFoundException() {
        super(HttpStatus.NOT_FOUND, "CALL_002", "통화 참여자를 찾을 수 없습니다.");
    }
}
