package chainsawman.gesture.exceptions.friend;

import chainsawman.gesture.exceptions.common.DomainException;
import org.springframework.http.HttpStatus;

public class FriendshipNotFoundException extends DomainException {
    public FriendshipNotFoundException() {
        super(HttpStatus.NOT_FOUND, "FRIEND_001", "친구 관계를 찾을 수 없습니다.");
    }
}
