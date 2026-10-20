package chainsawman.gesture.repository.room;

import chainsawman.gesture.entity.room.Room;
import chainsawman.gesture.enums.RoomType;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface RoomRepository extends JpaRepository<Room, Long> {

    Optional<Room> findByIdxAndDeletedAtIsNull(Long idx);

    Page<Room> findByDeletedAtIsNull(Pageable pageable);

    Page<Room> findByCategoryAndDeletedAtIsNull(RoomType category, Pageable pageable);

    Page<Room> findByTitleContainingIgnoreCaseAndDeletedAtIsNull(String title, Pageable pageable);

    Page<Room> findByTitleContainingIgnoreCaseAndCategoryAndDeletedAtIsNull(String title, RoomType category, Pageable pageable);

    List<Room> findAllByHost_IdxAndDeletedAtIsNull(Long hostIdx);
}
