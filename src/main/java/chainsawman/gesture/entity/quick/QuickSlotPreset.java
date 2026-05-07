package chainsawman.gesture.entity.quick;

import chainsawman.gesture.entity.user.User;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.springframework.data.annotation.LastModifiedDate;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;
import java.util.stream.Stream;

@Entity
@Table(name = "quick_slot_presets")
@Getter
@Builder
@NoArgsConstructor
@AllArgsConstructor
@EntityListeners(AuditingEntityListener.class)
public class QuickSlotPreset {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long idx;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_idx", nullable = false)
    private User user;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "slot_id_1")
    private QuickSlot slot1;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "slot_id_2")
    private QuickSlot slot2;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "slot_id_3")
    private QuickSlot slot3;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "slot_id_4")
    private QuickSlot slot4;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "slot_id_5")
    private QuickSlot slot5;

    @LastModifiedDate
    @Column(name = "updated_at", nullable = false)
    private LocalDateTime updatedAt;

    public void updateSlots(List<QuickSlot> slots) {
        this.slot1 = slots.size() > 0 ? slots.get(0) : null;
        this.slot2 = slots.size() > 1 ? slots.get(1) : null;
        this.slot3 = slots.size() > 2 ? slots.get(2) : null;
        this.slot4 = slots.size() > 3 ? slots.get(3) : null;
        this.slot5 = slots.size() > 4 ? slots.get(4) : null;
    }

    public List<QuickSlot> getActiveSlots() {
        return Stream.of(slot1, slot2, slot3, slot4, slot5)
                .filter(Objects::nonNull)
                .collect(Collectors.toList());
    }

    public void removeSlot(Long slotIdx) {
        if (slot1 != null && slot1.getIdx().equals(slotIdx)) slot1 = null;
        if (slot2 != null && slot2.getIdx().equals(slotIdx)) slot2 = null;
        if (slot3 != null && slot3.getIdx().equals(slotIdx)) slot3 = null;
        if (slot4 != null && slot4.getIdx().equals(slotIdx)) slot4 = null;
        if (slot5 != null && slot5.getIdx().equals(slotIdx)) slot5 = null;
    }
}
