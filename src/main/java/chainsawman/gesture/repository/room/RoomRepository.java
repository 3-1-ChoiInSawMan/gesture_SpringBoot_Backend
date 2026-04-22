package chainsawman.gesture.repository.room;

import chainsawman.gesture.entity.room.Room;
import chainsawman.gesture.enums.RoomType;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface RoomRepository extends JpaRepository<Room, Long> {

    Page<Room> findByCategory(RoomType category, Pageable pageable);

    Page<Room> findByTitleContainingIgnoreCase(String title, Pageable pageable);

    Page<Room> findByTitleContainingIgnoreCaseAndCategory(String title, RoomType category, Pageable pageable);

    List<Room> findAllByHost_Idx(Long hostIdx);
}
