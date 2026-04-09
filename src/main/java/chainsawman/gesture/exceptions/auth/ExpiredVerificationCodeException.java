package chainsawman.gesture.exceptions.auth;

import chainsawman.gesture.exceptions.common.DomainException;
import org.springframework.http.HttpStatus;

public class ExpiredVerificationCodeException extends DomainException {
    public ExpiredVerificationCodeException() {
        super(HttpStatus.BAD_REQUEST, "AUTH_006", "만료된 인증 코드입니다.");
    }
}
