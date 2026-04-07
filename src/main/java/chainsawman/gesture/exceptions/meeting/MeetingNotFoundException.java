package chainsawman.gesture.exceptions.meeting;

import chainsawman.gesture.exceptions.common.DomainException;
import org.springframework.http.HttpStatus;

public class MeetingNotFoundException extends DomainException {
    public MeetingNotFoundException() {
        super(HttpStatus.NOT_FOUND, "MEETING_001", "회의록을 찾을 수 없습니다.");
    }
}
