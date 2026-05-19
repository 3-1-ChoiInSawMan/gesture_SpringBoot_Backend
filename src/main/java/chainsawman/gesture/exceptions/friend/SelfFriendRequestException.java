package chainsawman.gesture.exceptions.friend;

import chainsawman.gesture.exceptions.common.DomainException;
import org.springframework.http.HttpStatus;

public class SelfFriendRequestException extends DomainException {
    public SelfFriendRequestException() {
        super(HttpStatus.BAD_REQUEST, "FRIEND_007", "자기 자신에게 친구 요청을 보낼 수 없습니다.");
    }
}
