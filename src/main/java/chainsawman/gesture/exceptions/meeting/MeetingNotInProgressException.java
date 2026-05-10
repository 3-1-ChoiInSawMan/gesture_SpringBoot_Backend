package chainsawman.gesture.exceptions.meeting;

import chainsawman.gesture.exceptions.common.DomainException;
import org.springframework.http.HttpStatus;

public class MeetingNotInProgressException extends DomainException {
    public MeetingNotInProgressException() {
        super(HttpStatus.BAD_REQUEST, "MEETING_003", "진행 중인 회의록이 없습니다.");
    }
}
