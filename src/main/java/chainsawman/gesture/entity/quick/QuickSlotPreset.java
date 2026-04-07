package chainsawman.gesture.entity.quick;

import chainsawman.gesture.entity.user.User;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Entity
@Table(name = "quick_slot_presets")
@Getter
@Setter
@NoArgsConstructor
public class QuickSlotPreset {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long idx;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_idx", nullable = false)
    private User user;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "action_id_1")
    private QuickAction action1;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "action_id_2")
    private QuickAction action2;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "action_id_3")
    private QuickAction action3;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "action_id_4")
    private QuickAction action4;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "action_id_5")
    private QuickAction action5;
}
