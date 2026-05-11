package chainsawman.gesture.repository.call;

import chainsawman.gesture.entity.call.CallParticipant;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface CallParticipantRepository extends JpaRepository<CallParticipant, Long> {

    Optional<CallParticipant> findByCall_IdxAndUser_IdxAndLeftAtIsNull(Long callIdx, Long userIdx);

    boolean existsByCall_IdxAndUser_IdxAndLeftAtIsNull(Long callIdx, Long userIdx);

    List<CallParticipant> findByCall_IdxAndLeftAtIsNullOrderByJoinedAtAsc(Long callIdx);

    int countByCall_IdxAndLeftAtIsNull(Long callIdx);
}
