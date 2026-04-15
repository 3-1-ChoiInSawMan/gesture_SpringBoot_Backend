package chainsawman.gesture.repository.chat;

import chainsawman.gesture.entity.chat.ChatNotice;
import org.springframework.data.jpa.repository.JpaRepository;

public interface ChatNoticeRepository extends JpaRepository<ChatNotice, Long> {
}
