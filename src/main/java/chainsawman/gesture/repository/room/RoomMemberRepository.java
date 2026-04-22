package chainsawman.gesture.repository.room;

import chainsawman.gesture.entity.room.RoomMember;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface RoomMemberRepository extends JpaRepository<RoomMember, Long> {

    Optional<RoomMember> findByRoom_IdxAndUser_Idx(Long roomIdx, Long userIdx);

    boolean existsByRoom_IdxAndUser_Idx(Long roomIdx, Long userIdx);

    int countByRoom_Idx(Long roomIdx);

    void deleteAllByRoom_Idx(Long roomIdx);

    void deleteByRoom_IdxAndUser_Idx(Long roomIdx, Long userIdx);

    Optional<RoomMember> findTopByRoom_IdxAndUser_IdxNotOrderByCreatedAtAsc(Long roomIdx, Long userIdx);
}
