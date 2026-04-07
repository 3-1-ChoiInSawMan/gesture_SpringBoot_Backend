package chainsawman.gesture.exceptions.friend;

import chainsawman.gesture.exceptions.common.DomainException;
import org.springframework.http.HttpStatus;

public class FriendRequestNotFoundException extends DomainException {
    public FriendRequestNotFoundException() {
        super(HttpStatus.NOT_FOUND, "FRIEND_003", "친구 요청을 찾을 수 없습니다.");
    }
}
