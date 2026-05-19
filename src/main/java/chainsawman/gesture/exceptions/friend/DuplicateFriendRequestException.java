package chainsawman.gesture.exceptions.friend;

import chainsawman.gesture.exceptions.common.DomainException;
import org.springframework.http.HttpStatus;

public class DuplicateFriendRequestException extends DomainException {
    public DuplicateFriendRequestException() {
        super(HttpStatus.CONFLICT, "FRIEND_006", "이미 친구 요청을 보낸 대상입니다.");
    }
}
