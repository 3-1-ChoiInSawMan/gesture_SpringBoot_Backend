package chainsawman.gesture.exceptions.notification;

import chainsawman.gesture.exceptions.common.DomainException;
import org.springframework.http.HttpStatus;

public class NotificationNotFoundException extends DomainException {
    public NotificationNotFoundException() {
        super(HttpStatus.NOT_FOUND, "NOTIFICATION_001", "알림을 찾을 수 없습니다.");
    }
}
