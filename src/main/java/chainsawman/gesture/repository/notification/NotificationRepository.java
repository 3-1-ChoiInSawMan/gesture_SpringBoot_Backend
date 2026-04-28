package chainsawman.gesture.repository.notification;

import chainsawman.gesture.entity.notification.Notification;
import chainsawman.gesture.entity.user.User;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface NotificationRepository extends JpaRepository<Notification, Long> {

    List<Notification> findByUserOrderByCreatedAtDesc(User user);
}
