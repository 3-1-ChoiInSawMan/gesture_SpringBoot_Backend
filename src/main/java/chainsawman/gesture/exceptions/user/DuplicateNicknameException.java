package chainsawman.gesture.exceptions.user;

import chainsawman.gesture.exceptions.common.DomainException;
import org.springframework.http.HttpStatus;

public class DuplicateNicknameException extends DomainException {
    public DuplicateNicknameException() {
        super(HttpStatus.CONFLICT, "USER_004", "이미 사용 중인 닉네임입니다.");
    }
}
