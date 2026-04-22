package chainsawman.gesture.exceptions.room;

import chainsawman.gesture.exceptions.common.DomainException;
import org.springframework.http.HttpStatus;

public class RoomMaxParticipantExceededException extends DomainException {
    public RoomMaxParticipantExceededException(int currentCount) {
        super(HttpStatus.BAD_REQUEST, "ROOM_005", "최대 인원은 현재 참여 인원(" + currentCount + "명)보다 적을 수 없습니다.");
    }
}
