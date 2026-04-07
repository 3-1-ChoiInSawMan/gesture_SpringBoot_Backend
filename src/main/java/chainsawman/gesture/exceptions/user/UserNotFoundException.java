package chainsawman.gesture.exceptions.user;

import chainsawman.gesture.exceptions.common.DomainException;
import org.springframework.http.HttpStatus;

public class UserNotFoundException extends DomainException {
    public UserNotFoundException() {
        super(HttpStatus.NOT_FOUND, "USER_001", "유저를 찾을 수 없습니다.");
    }
}
