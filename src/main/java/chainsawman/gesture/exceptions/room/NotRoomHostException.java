package chainsawman.gesture.exceptions.room;

import chainsawman.gesture.exceptions.common.DomainException;
import org.springframework.http.HttpStatus;

public class NotRoomHostException extends DomainException {
    public NotRoomHostException() {
        super(HttpStatus.FORBIDDEN, "ROOM_007", "통화방 호스트만 가능한 기능입니다.");
    }
}
