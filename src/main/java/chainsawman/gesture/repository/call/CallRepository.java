package chainsawman.gesture.repository.call;

import chainsawman.gesture.entity.call.Call;
import org.springframework.data.jpa.repository.JpaRepository;

public interface CallRepository extends JpaRepository<Call, Long> {
}
