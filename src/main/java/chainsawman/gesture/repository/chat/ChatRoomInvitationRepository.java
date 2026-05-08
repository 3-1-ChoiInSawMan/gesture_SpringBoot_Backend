package chainsawman.gesture.repository.chat;

import chainsawman.gesture.entity.chat.ChatRoomInvitation;
import chainsawman.gesture.enums.InvitationStatus;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface ChatRoomInvitationRepository extends JpaRepository<ChatRoomInvitation, Long> {

    Optional<ChatRoomInvitation> findByIdxAndInvitee_Idx(Long idx, Long inviteeIdx);

    boolean existsByChatRoom_IdxAndInvitee_IdxAndStatus(Long chatRoomIdx, Long inviteeIdx, InvitationStatus status);
}
