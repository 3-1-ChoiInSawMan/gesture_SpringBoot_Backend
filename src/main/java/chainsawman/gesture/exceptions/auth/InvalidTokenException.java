package chainsawman.gesture.exceptions.auth;

import chainsawman.gesture.exceptions.common.DomainException;
import org.springframework.http.HttpStatus;

public class InvalidTokenException extends DomainException {
    public InvalidTokenException() {
        super(HttpStatus.UNAUTHORIZED, "AUTH_001", "유효하지 않은 토큰입니다.");
    }
}
