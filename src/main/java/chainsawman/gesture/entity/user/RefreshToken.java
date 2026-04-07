package chainsawman.gesture.entity.user;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDateTime;

@Entity
@Table(name = "refresh_tokens")
@Getter
@Setter
@NoArgsConstructor
public class RefreshToken {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long idx;

    @OneToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_idx", nullable = false, unique = true)
    private User user;

    @Column(nullable = false, columnDefinition = "TEXT")
    private String token;

    @Column(name = "expires_at", nullable = false)
    private LocalDateTime expiresAt;
}
