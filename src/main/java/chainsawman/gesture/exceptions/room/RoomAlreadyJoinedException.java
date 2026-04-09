package chainsawman.gesture.exceptions.room;

import chainsawman.gesture.exceptions.common.DomainException;
import org.springframework.http.HttpStatus;

public class RoomAlreadyJoinedException extends DomainException {
    public RoomAlreadyJoinedException() {
        super(HttpStatus.CONFLICT, "ROOM_003", "이미 참여 중인 통화방입니다.");
    }
}
