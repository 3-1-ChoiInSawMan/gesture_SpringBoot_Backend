package chainsawman.gesture.entity.friend;

import chainsawman.gesture.entity.user.User;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

import java.time.LocalDateTime;

@Entity
@Table(name = "friendships")
@Getter
@Setter
@NoArgsConstructor
@EntityListeners(AuditingEntityListener.class)
public class Friendship {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long idx;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_idx", nullable = false)
    private User user;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "friend_idx", nullable = false)
    private User friend;

    @Column(nullable = false)
    private boolean accept = false;

    @Column(name = "not_accept", nullable = false)
    private boolean notAccept = false;

    @CreatedDate
    @Column(name = "request_at", nullable = false, updatable = false)
    private LocalDateTime requestAt;
}
