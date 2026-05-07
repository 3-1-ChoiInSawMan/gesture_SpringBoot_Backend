package chainsawman.gesture.entity.media;

import chainsawman.gesture.entity.user.User;
import chainsawman.gesture.enums.MediaEntityType;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Entity
@Table(name = "media")
@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class Media {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long idx;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_idx", nullable = false)
    private User user;

    @Enumerated(EnumType.STRING)
    @Column(name = "entity_type", length = 20)
    private MediaEntityType entityType;

    @Column(columnDefinition = "TEXT")
    private String file;

    @Column(unique = true, nullable = false)
    private String uuid;

    @Column(nullable = false, length = 140)
    private String name;
}
