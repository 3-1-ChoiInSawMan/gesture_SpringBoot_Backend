package chainsawman.gesture.repository.friend;

import chainsawman.gesture.entity.friend.Friendship;
import org.springframework.data.jpa.repository.JpaRepository;

public interface FriendshipRepository extends JpaRepository<Friendship, Long> {
}
