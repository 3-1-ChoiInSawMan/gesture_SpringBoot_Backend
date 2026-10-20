package chainsawman.gesture.repository.meeting;

import chainsawman.gesture.entity.meeting.Meeting;
import chainsawman.gesture.enums.MeetingStatus;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface MeetingRepository extends JpaRepository<Meeting, Long> {

    List<Meeting> findByCall_Room_IdxOrderByStartedAtDesc(Long roomIdx);

    Optional<Meeting> findByCall_IdxAndStatus(Long callIdx, MeetingStatus status);

    boolean existsByCall_IdxAndStatus(Long callIdx, MeetingStatus status);
}
