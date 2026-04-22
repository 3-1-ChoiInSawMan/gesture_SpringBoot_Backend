package chainsawman.gesture.exceptions.friend;

import chainsawman.gesture.exceptions.common.DomainException;
import org.springframework.http.HttpStatus;

public class InvalidFriendRequestStatusException extends DomainException {
    public InvalidFriendRequestStatusException() {
        super(HttpStatus.BAD_REQUEST, "FRIEND_004", "status는 ACCEPTED 또는 REJECTED만 가능합니다.");
    }
}
