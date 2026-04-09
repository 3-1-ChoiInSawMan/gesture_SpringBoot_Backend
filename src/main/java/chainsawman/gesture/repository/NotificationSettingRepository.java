package chainsawman.gesture.repository;

import chainsawman.gesture.entity.notification.NotificationSetting;
import org.springframework.data.jpa.repository.JpaRepository;

public interface NotificationSettingRepository extends JpaRepository<NotificationSetting, Long> {
}
