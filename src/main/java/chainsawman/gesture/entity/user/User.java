package chainsawman.gesture.entity.user;

import chainsawman.gesture.enums.ProviderType;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.annotation.LastModifiedDate;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

import java.time.LocalDateTime;

@Entity
@Table(name = "users")
@Getter
@Setter
@NoArgsConstructor
@EntityListeners(AuditingEntityListener.class)
public class User {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long idx;

    @Column(nullable = false, unique = true, length = 255)
    private String email;

    @Column(nullable = false, length = 500)
    private String nickname;

    @Column(nullable = false, unique = true, length = 200)
    private String id;

    @Column(columnDefinition = "TEXT")
    private String password;

    @Column(name = "status_message", columnDefinition = "TEXT")
    private String statusMessage;

    @CreatedDate
    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @LastModifiedDate
    @Column(name = "updated_at", nullable = false)
    private LocalDateTime updatedAt;

    @Column(length = 200)
    @Enumerated(EnumType.STRING)
    private ProviderType provider;

    @Column(name = "is_deactivated", nullable = false)
    private Boolean isDeactivated = false;
}
