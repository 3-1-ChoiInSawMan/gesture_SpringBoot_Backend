package chainsawman.gesture.exceptions.auth;

import chainsawman.gesture.exceptions.common.DomainException;
import org.springframework.http.HttpStatus;

public class ExpiredTokenException extends DomainException {
    public ExpiredTokenException() {
        super(HttpStatus.UNAUTHORIZED, "AUTH_002", "만료된 토큰입니다.");
    }
}
