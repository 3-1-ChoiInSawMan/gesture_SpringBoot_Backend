package chainsawman.gesture.repository.chat;

import chainsawman.gesture.entity.chat.ChatParticipant;
import org.springframework.data.jpa.repository.JpaRepository;

public interface ChatParticipantRepository extends JpaRepository<ChatParticipant, Long> {

    void deleteByUser_IdxAndChatRoom_Idx(Long userIdx, Long chatRoomIdx);
}
