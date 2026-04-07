package chainsawman.gesture.entity.chat;

import chainsawman.gesture.entity.user.User;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Entity
@Table(name = "chat_participants",
        uniqueConstraints = @UniqueConstraint(
                name = "uk_chat_participants_chat_user",
                columnNames = {"chat_idx", "user_idx"}
        ),
        indexes = @Index(name = "idx_chat_participants_chat", columnList = "chat_idx"))
@Getter
@Setter
@NoArgsConstructor
public class ChatParticipant {

    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long idx;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "chat_idx", nullable = false)
    private ChatRoom chatRoom;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_idx")
    private User user;
}
