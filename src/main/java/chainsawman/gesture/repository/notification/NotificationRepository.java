package chainsawman.gesture.repository.notification;

import chainsawman.gesture.entity.notification.Notification;
import org.springframework.data.jpa.repository.JpaRepository;

public interface NotificationRepository extends JpaRepository<Notification, Long> {
}
