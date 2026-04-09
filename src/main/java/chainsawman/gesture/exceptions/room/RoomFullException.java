package chainsawman.gesture.exceptions.room;

import chainsawman.gesture.exceptions.common.DomainException;
import org.springframework.http.HttpStatus;

public class RoomFullException extends DomainException {
    public RoomFullException() {
        super(HttpStatus.BAD_REQUEST, "ROOM_004", "통화방 정원이 초과되었습니다.");
    }
}
