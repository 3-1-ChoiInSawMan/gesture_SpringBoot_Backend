package chainsawman.gesture.exceptions.auth;

import chainsawman.gesture.exceptions.common.DomainException;
import org.springframework.http.HttpStatus;

public class ExpiredRefreshTokenException extends DomainException {
    public ExpiredRefreshTokenException() {
        super(HttpStatus.UNAUTHORIZED, "AUTH_004", "만료된 리프레시 토큰입니다.");
    }
}
