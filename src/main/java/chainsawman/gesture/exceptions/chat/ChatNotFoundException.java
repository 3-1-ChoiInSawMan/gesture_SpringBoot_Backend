package chainsawman.gesture.exceptions.chat;

import chainsawman.gesture.exceptions.common.DomainException;
import org.springframework.http.HttpStatus;

public class ChatNotFoundException extends DomainException {
    public ChatNotFoundException() {
        super(HttpStatus.NOT_FOUND, "CHAT_002", "채팅 메시지를 찾을 수 없습니다.");
    }
}
