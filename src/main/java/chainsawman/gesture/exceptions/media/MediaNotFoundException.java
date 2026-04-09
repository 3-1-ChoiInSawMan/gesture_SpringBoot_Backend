package chainsawman.gesture.exceptions.media;

import chainsawman.gesture.exceptions.common.DomainException;
import org.springframework.http.HttpStatus;

public class MediaNotFoundException extends DomainException {
    public MediaNotFoundException() {
        super(HttpStatus.NOT_FOUND, "MEDIA_001", "미디어 파일을 찾을 수 없습니다.");
    }
}
