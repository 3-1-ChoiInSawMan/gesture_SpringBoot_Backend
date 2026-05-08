package chainsawman.gesture.exceptions.chat;

import chainsawman.gesture.exceptions.common.DomainException;
import org.springframework.http.HttpStatus;

public class ChatRoomInvitationAlreadyRespondedException extends DomainException {
    public ChatRoomInvitationAlreadyRespondedException() {
        super(HttpStatus.BAD_REQUEST, "CHAT_003", "이미 처리된 초대입니다.");
    }
}
