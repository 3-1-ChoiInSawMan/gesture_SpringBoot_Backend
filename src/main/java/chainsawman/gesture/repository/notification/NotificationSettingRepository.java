package chainsawman.gesture.repository.notification;

import chainsawman.gesture.entity.notification.NotificationSetting;
import chainsawman.gesture.entity.user.User;
import chainsawman.gesture.enums.NotificationType;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface NotificationSettingRepository extends JpaRepository<NotificationSetting, Long> {

    List<NotificationSetting> findByUser(User user);

    Optional<NotificationSetting> findByUserAndType(User user, NotificationType type);
}
