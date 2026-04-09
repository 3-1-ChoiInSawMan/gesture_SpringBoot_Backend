package chainsawman.gesture.exceptions.user;

import chainsawman.gesture.exceptions.common.DomainException;
import org.springframework.http.HttpStatus;

public class InvalidPasswordException extends DomainException {
    public InvalidPasswordException() {
        super(HttpStatus.BAD_REQUEST, "USER_003", "비밀번호가 올바르지 않습니다.");
    }
}
