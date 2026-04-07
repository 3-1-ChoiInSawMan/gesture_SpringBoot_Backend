package chainsawman.gesture.exceptions.auth;

import chainsawman.gesture.exceptions.common.DomainException;
import org.springframework.http.HttpStatus;

public class InvalidVerificationCodeException extends DomainException {
    public InvalidVerificationCodeException() {
        super(HttpStatus.BAD_REQUEST, "AUTH_005", "유효하지 않은 인증 코드입니다.");
    }
}
