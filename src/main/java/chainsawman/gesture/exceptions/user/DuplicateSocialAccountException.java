package chainsawman.gesture.exceptions.user;

import chainsawman.gesture.exceptions.common.DomainException;
import org.springframework.http.HttpStatus;

public class DuplicateSocialAccountException extends DomainException {
    public DuplicateSocialAccountException() {
        super(HttpStatus.CONFLICT, "USER_006", "이미 가입된 소셜 계정입니다.");
    }
}
