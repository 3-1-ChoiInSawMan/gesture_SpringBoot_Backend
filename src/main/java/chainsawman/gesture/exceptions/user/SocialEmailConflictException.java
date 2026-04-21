package chainsawman.gesture.exceptions.user;

import chainsawman.gesture.exceptions.common.DomainException;
import org.springframework.http.HttpStatus;

public class SocialEmailConflictException extends DomainException {
    public SocialEmailConflictException() {
        super(HttpStatus.CONFLICT, "USER_007", "해당 이메일은 이미 다른 소셜 계정과 연결되어 있습니다.");
    }
}
