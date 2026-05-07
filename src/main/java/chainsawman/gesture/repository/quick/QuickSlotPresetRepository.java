package chainsawman.gesture.repository.quick;

import chainsawman.gesture.entity.quick.QuickSlotPreset;
import chainsawman.gesture.entity.user.User;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface QuickSlotPresetRepository extends JpaRepository<QuickSlotPreset, Long> {
    Optional<QuickSlotPreset> findByUser(User user);
}
