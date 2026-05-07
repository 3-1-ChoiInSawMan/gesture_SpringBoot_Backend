package chainsawman.gesture.repository.media;

import chainsawman.gesture.entity.media.Media;
import chainsawman.gesture.enums.MediaEntityType;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Collection;
import java.util.List;
import java.util.Optional;


public interface MediaRepository extends JpaRepository<Media, Long> {
    Optional<Media> findByUuid(String uuid);

    List<Media> findByUuidIn(Collection<String> uuids);

    Optional<Media> findFirstByUser_IdxAndEntityType(Long userIdx, MediaEntityType entityType);

    List<Media> findByUser_IdInAndEntityType(List<String> ids, MediaEntityType entityType);
}
