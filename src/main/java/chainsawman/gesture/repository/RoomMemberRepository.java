package chainsawman.gesture.repository;

import chainsawman.gesture.entity.room.RoomMember;
import org.springframework.data.jpa.repository.JpaRepository;

public interface RoomMemberRepository extends JpaRepository<RoomMember, Long> {
}
