package chainsawman.gesture.entity.room;

import chainsawman.gesture.entity.user.User;
import chainsawman.gesture.enums.RoomType;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.annotation.LastModifiedDate;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

import java.time.LocalDateTime;

@Entity
@Table(name = "rooms",
        indexes = @Index(name = "idx_rooms_public_created", columnList = "is_public, created_at"))
@Getter
@Setter
@NoArgsConstructor
@EntityListeners(AuditingEntityListener.class)
public class Room {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long idx;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "host_idx", nullable = false)
    private User host;

    @Column(nullable = false, columnDefinition = "TEXT")
    private String title;

    @Column(name = "max_participant", nullable = false)
    private int maxParticipant;

    @Column(name = "is_public", nullable = false)
    private boolean isPublic = true;

    @Column(length = 255)
    private String password;

    @Column(name = "thumbnail_url", length = 255)
    private String thumbnailUrl;

    @Column(length = 50)
    @Enumerated(EnumType.STRING)
    private RoomType category;

    @CreatedDate
    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @LastModifiedDate
    @Column(name = "updated_at", nullable = false)
    private LocalDateTime updatedAt;
}
