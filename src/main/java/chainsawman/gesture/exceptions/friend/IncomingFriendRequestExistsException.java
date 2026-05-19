package chainsawman.gesture.exceptions.friend;

import chainsawman.gesture.exceptions.common.DomainException;
import org.springframework.http.HttpStatus;

public class IncomingFriendRequestExistsException extends DomainException {
    public IncomingFriendRequestExistsException() {
        super(HttpStatus.CONFLICT, "FRIEND_008", "상대방이 이미 친구 요청을 보낸 상태입니다.");
    }
}
