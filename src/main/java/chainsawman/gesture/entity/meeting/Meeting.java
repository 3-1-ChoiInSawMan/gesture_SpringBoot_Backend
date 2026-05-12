package chainsawman.gesture.entity.meeting;

import chainsawman.gesture.entity.call.Call;
import chainsawman.gesture.entity.room.Room;
import chainsawman.gesture.entity.user.User;
import chainsawman.gesture.enums.MeetingStatus;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.annotation.LastModifiedDate;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "meetings")
@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
@EntityListeners(AuditingEntityListener.class)
public class Meeting {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long idx;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "call_idx", nullable = false)
    private Call call;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "room_idx", nullable = false)
    private Room room;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "created_by", nullable = false)
    private User createdBy;

    @Column(length = 100)
    private String title;

    @Column(columnDefinition = "TEXT")
    private String content;

    @Column(name = "ai_summary", columnDefinition = "TEXT")
    private String aiSummary;

    @Builder.Default
    @ElementCollection
    @CollectionTable(name = "meeting_participants", joinColumns = @JoinColumn(name = "meeting_idx"))
    @Column(name = "participant_name", length = 100)
    private List<String> participants = new ArrayList<>();

    @Builder.Default
    @ElementCollection
    @CollectionTable(name = "meeting_conclusions", joinColumns = @JoinColumn(name = "meeting_idx"))
    @Column(name = "conclusion", columnDefinition = "TEXT")
    private List<String> conclusion = new ArrayList<>();

    @Enumerated(EnumType.STRING)
    @Column(length = 20, nullable = false)
    private MeetingStatus status;

    @Column(name = "started_at")
    private LocalDateTime startedAt;

    @Column(name = "ended_at")
    private LocalDateTime endedAt;

    @CreatedDate
    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @LastModifiedDate
    @Column(name = "updated_at", nullable = false)
    private LocalDateTime updatedAt;
}
