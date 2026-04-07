package chainsawman.gesture.exceptions.friend;

import chainsawman.gesture.exceptions.common.DomainException;
import org.springframework.http.HttpStatus;

public class AlreadyFriendException extends DomainException {
    public AlreadyFriendException() {
        super(HttpStatus.CONFLICT, "FRIEND_002", "이미 친구 관계입니다.");
    }
}
