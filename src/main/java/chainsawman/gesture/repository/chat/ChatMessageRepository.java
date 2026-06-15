package chainsawman.gesture.repository.chat;

import chainsawman.gesture.entity.chat.ChatMessage;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface ChatMessageRepository extends JpaRepository<ChatMessage, Long> {

    List<ChatMessage> findByChatRoom_IdxOrderByIdxDesc(Long chatRoomIdx, Pageable pageable);

    List<ChatMessage> findByChatRoom_IdxAndIdxLessThanOrderByIdxDesc(Long chatRoomIdx, Long cursorIdx, Pageable pageable);

    boolean existsByChatRoom_IdxAndIdx(Long chatRoomIdx, Long messageIdx);
}
