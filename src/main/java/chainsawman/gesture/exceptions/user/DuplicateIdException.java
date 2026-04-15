package chainsawman.gesture.exceptions.user;

import chainsawman.gesture.exceptions.common.DomainException;
import org.springframework.http.HttpStatus;


public class DuplicateIdException extends DomainException {
    public DuplicateIdException() {
        super(HttpStatus.CONFLICT, "USER_004", "이미 사용 중인 아이디입니다.");
    }
}
