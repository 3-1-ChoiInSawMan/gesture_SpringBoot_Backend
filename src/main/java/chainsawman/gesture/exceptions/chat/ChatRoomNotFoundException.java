package chainsawman.gesture.exceptions.chat;

import chainsawman.gesture.exceptions.common.DomainException;
import org.springframework.http.HttpStatus;

public class ChatRoomNotFoundException extends DomainException {
    public ChatRoomNotFoundException() {
        super(HttpStatus.NOT_FOUND, "CHAT_001", "채팅방을 찾을 수 없습니다.");
    }
}
