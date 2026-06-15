package chainsawman.gesture.exceptions.chat;

import chainsawman.gesture.exceptions.common.DomainException;
import org.springframework.http.HttpStatus;

public class ChatRoomNotParticipantException extends DomainException {
    public ChatRoomNotParticipantException() {
        super(HttpStatus.FORBIDDEN, "CHAT_004", "채팅방 참여자가 아닙니다.");
    }
}
