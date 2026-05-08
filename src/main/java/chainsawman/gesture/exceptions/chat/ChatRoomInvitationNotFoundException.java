package chainsawman.gesture.exceptions.chat;

import chainsawman.gesture.exceptions.common.DomainException;
import org.springframework.http.HttpStatus;

public class ChatRoomInvitationNotFoundException extends DomainException {
    public ChatRoomInvitationNotFoundException() {
        super(HttpStatus.NOT_FOUND, "CHAT_002", "채팅방 초대를 찾을 수 없습니다.");
    }
}
