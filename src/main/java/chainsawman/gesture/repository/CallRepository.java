package chainsawman.gesture.repository;

import chainsawman.gesture.entity.call.Call;
import org.springframework.data.jpa.repository.JpaRepository;

public interface CallRepository extends JpaRepository<Call, Long> {
}
