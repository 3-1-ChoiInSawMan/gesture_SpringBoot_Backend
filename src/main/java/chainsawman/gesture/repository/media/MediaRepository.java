package chainsawman.gesture.repository.media;

import chainsawman.gesture.entity.media.Media;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface MediaRepository extends JpaRepository<Media, Long> {
    Optional<Media> findByUser_Idx(Long userIdx);

    List<Media> findByUser_IdIn(List<String> ids);
}
