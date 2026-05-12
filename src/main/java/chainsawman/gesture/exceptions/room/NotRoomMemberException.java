package chainsawman.gesture.exceptions.room;

import chainsawman.gesture.exceptions.common.DomainException;
import org.springframework.http.HttpStatus;


public class NotRoomMemberException extends DomainException {
    public NotRoomMemberException() {
        super(HttpStatus.FORBIDDEN, "ROOM_006", "해당 방의 멤버만 가능한 권한입니다.");
    }
}
