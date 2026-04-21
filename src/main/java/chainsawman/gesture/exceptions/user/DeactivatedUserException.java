package chainsawman.gesture.exceptions.user;

import chainsawman.gesture.exceptions.common.DomainException;
import org.springframework.http.HttpStatus;

public class DeactivatedUserException extends DomainException {
    public DeactivatedUserException() {
        super(HttpStatus.FORBIDDEN, "USER_005", "탈퇴된 유저입니다.");
    }
}
