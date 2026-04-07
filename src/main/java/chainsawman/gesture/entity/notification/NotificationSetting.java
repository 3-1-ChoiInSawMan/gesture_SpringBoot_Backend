package chainsawman.gesture.entity.notification;

import chainsawman.gesture.entity.user.User;
import chainsawman.gesture.enums.NotificationType;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Entity
@Table(name = "notification_settings")
@Getter
@Setter
@NoArgsConstructor
public class NotificationSetting {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long idx;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    @Column(nullable = false, length = 100)
    @Enumerated(EnumType.STRING)
    private NotificationType type;

    @Column(name = "is_used", nullable = false)
    private boolean isUsed = true;
}
