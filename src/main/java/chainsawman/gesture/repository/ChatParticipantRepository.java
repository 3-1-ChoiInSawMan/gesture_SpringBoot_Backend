package chainsawman.gesture.repository;

import chainsawman.gesture.entity.chat.ChatParticipant;
import org.springframework.data.jpa.repository.JpaRepository;

public interface ChatParticipantRepository extends JpaRepository<ChatParticipant, Long> {
}
