package chainsawman.gesture.exceptions.call;

import chainsawman.gesture.exceptions.common.DomainException;
import org.springframework.http.HttpStatus;

public class NoActiveCallException extends DomainException {
    public NoActiveCallException() {
        super(HttpStatus.NOT_FOUND, "CALL_003", "진행 중인 통화가 없습니다.");
    }
}
