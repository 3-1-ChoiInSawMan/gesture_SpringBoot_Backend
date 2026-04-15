package chainsawman.gesture.exceptions.auth;

import chainsawman.gesture.exceptions.common.DomainException;
import org.springframework.http.HttpStatus;


public class NotAuthenticatedException extends DomainException {
    public NotAuthenticatedException() {
        super(HttpStatus.UNAUTHORIZED, "AUTH_007", "로그인이 필요합니다.");
    }
}
