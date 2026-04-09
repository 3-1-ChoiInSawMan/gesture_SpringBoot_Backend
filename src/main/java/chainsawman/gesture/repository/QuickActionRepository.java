package chainsawman.gesture.repository;

import chainsawman.gesture.entity.quick.QuickAction;
import org.springframework.data.jpa.repository.JpaRepository;

public interface QuickActionRepository extends JpaRepository<QuickAction, Long> {
}
