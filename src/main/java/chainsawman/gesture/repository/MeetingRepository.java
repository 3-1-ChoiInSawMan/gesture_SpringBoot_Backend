package chainsawman.gesture.repository;

import chainsawman.gesture.entity.meeting.Meeting;
import org.springframework.data.jpa.repository.JpaRepository;

public interface MeetingRepository extends JpaRepository<Meeting, Long> {
}
