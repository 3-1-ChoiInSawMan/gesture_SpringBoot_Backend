package chainsawman.gesture.exceptions.meeting;

import chainsawman.gesture.exceptions.common.DomainException;
import org.springframework.http.HttpStatus;

public class MeetingAlreadyStartedException extends DomainException {
    public MeetingAlreadyStartedException() {
        super(HttpStatus.CONFLICT, "MEETING_002", "이미 진행 중인 회의록이 있습니다.");
    }
}
