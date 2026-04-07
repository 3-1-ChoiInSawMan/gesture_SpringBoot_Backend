package chainsawman.gesture.entity.chat;

import chainsawman.gesture.entity.media.Media;
import chainsawman.gesture.entity.user.User;
import chainsawman.gesture.enums.MessageType;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

import java.time.LocalDateTime;

@Entity
@Table(name = "chat_messages",
        indexes = @Index(name = "idx_chat_messages_chat_created", columnList = "chat_idx, created_at"))
@Getter
@Setter
@NoArgsConstructor
@EntityListeners(AuditingEntityListener.class)
public class ChatMessage {



    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long idx;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "sender_idx", nullable = false)
    private User sender;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "chat_idx", nullable = false)
    private ChatRoom chatRoom;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "file_url")
    private Media file;

    @Column(name = "user_idx")
    private Long userIdx;

    @Column(name = "target_idx")
    private Long targetIdx;

    @Column(columnDefinition = "TEXT")
    private String message;

    @Column(name = "is_deleted", nullable = false)
    private boolean isDeleted = false;

    @CreatedDate
    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @Column(name = "updated_at")
    private LocalDateTime updatedAt;

    @Column(nullable = false)
    @Enumerated(EnumType.STRING)
    private MessageType type;
}
