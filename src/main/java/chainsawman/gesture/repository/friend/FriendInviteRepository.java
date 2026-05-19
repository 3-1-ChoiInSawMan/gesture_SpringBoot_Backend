package chainsawman.gesture.repository.friend;

import chainsawman.gesture.entity.friend.FriendInvite;
import chainsawman.gesture.enums.InvitationStatus;
import org.springframework.data.jpa.repository.JpaRepository;

public interface FriendInviteRepository extends JpaRepository<FriendInvite, Long> {

    boolean existsBySender_IdxAndReceiver_IdxAndRoom_IdxAndStatus(
            Long senderIdx, Long receiverIdx, Long roomIdx, InvitationStatus status);
}

