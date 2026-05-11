package chainsawman.gesture.repository.call;

import chainsawman.gesture.entity.call.Call;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface CallRepository extends JpaRepository<Call, Long> {

    Optional<Call> findByRoom_IdxAndEndedAtIsNull(Long roomIdx);

    boolean existsByRoom_IdxAndEndedAtIsNull(Long roomIdx);
}
