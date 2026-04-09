package chainsawman.gesture.repository;

import chainsawman.gesture.entity.chat.ChatNotice;
import org.springframework.data.jpa.repository.JpaRepository;

public interface ChatNoticeRepository extends JpaRepository<ChatNotice, Long> {
}
