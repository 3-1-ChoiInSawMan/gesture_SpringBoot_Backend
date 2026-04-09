package chainsawman.gesture.repository;

import chainsawman.gesture.entity.call.CallParticipant;
import org.springframework.data.jpa.repository.JpaRepository;

public interface CallParticipantRepository extends JpaRepository<CallParticipant, Long> {
}
