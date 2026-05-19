package chainsawman.gesture.exceptions.friend;

import chainsawman.gesture.exceptions.common.DomainException;
import org.springframework.http.HttpStatus;

public class DuplicateFriendInviteException extends DomainException {
    public DuplicateFriendInviteException() {
        super(HttpStatus.CONFLICT, "FRIEND_005", "이미 해당 통화방에 초대 요청이 존재합니다.");
    }
}
