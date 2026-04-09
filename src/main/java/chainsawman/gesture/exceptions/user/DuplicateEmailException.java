package chainsawman.gesture.exceptions.user;

import chainsawman.gesture.exceptions.common.DomainException;
import org.springframework.http.HttpStatus;

public class DuplicateEmailException extends DomainException {
    public DuplicateEmailException() {
        super(HttpStatus.CONFLICT, "USER_002", "이미 사용 중인 이메일입니다.");
    }
}
