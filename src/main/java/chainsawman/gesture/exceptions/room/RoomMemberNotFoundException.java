package chainsawman.gesture.exceptions.room;

import chainsawman.gesture.exceptions.common.DomainException;
import org.springframework.http.HttpStatus;

public class RoomMemberNotFoundException extends DomainException {
    public RoomMemberNotFoundException() {
        super(HttpStatus.NOT_FOUND, "ROOM_002", "통화방 멤버를 찾을 수 없습니다.");
    }
}

