package chainsawman.gesture.exceptions.room;

import chainsawman.gesture.exceptions.common.DomainException;
import org.springframework.http.HttpStatus;

public class RoomNotFoundException extends DomainException {
    public RoomNotFoundException() {
        super(HttpStatus.NOT_FOUND, "ROOM_001", "통화방을 찾을 수 없습니다.");
    }
}
