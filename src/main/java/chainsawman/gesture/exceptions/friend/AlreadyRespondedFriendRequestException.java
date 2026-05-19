package chainsawman.gesture.exceptions.friend;

import chainsawman.gesture.exceptions.common.DomainException;
import org.springframework.http.HttpStatus;

public class AlreadyRespondedFriendRequestException extends DomainException {
    public AlreadyRespondedFriendRequestException() {
        super(HttpStatus.CONFLICT, "FRIEND_009", "이미 처리된 친구 요청입니다.");
    }
}
