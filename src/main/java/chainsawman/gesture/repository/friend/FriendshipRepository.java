package chainsawman.gesture.repository.friend;

import chainsawman.gesture.entity.friend.Friendship;
import chainsawman.gesture.enums.FriendshipStatus;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface FriendshipRepository extends JpaRepository<Friendship, Long> {

    List<Friendship> findAllByFriend_IdxAndStatus(Long friendIdx, FriendshipStatus status);

    List<Friendship> findAllByUser_IdxAndStatus(Long userIdx, FriendshipStatus status);

    long countByUser_IdxAndStatus(Long userIdx, FriendshipStatus status);

    long countByFriend_IdxAndStatus(Long friendIdx, FriendshipStatus status);

    Optional<Friendship> findByIdxAndFriend_Idx(Long idx, Long friendIdx);

    Optional<Friendship> findByUser_IdxAndFriend_IdxAndStatus(Long userIdx, Long friendIdx, FriendshipStatus status);
}
