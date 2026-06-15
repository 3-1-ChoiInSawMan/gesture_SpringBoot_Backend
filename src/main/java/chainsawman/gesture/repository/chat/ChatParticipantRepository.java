package chainsawman.gesture.repository.chat;

import chainsawman.gesture.entity.chat.ChatParticipant;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;

public interface ChatParticipantRepository extends JpaRepository<ChatParticipant, Long> {

    void deleteByUser_IdxAndChatRoom_Idx(Long userIdx, Long chatRoomIdx);

    List<ChatParticipant> findByChatRoom_Idx(Long chatRoomIdx);

    Optional<ChatParticipant> findByChatRoom_IdxAndUser_Idx(Long chatRoomIdx, Long userIdx);

    boolean existsByChatRoom_IdxAndUser_Idx(Long chatRoomIdx, Long userIdx);

    List<ChatParticipant> findByUser_Idx(Long userIdx);

    @Query("SELECT cp FROM ChatParticipant cp JOIN FETCH cp.chatRoom WHERE cp.user.idx = :userIdx")
    List<ChatParticipant> findByUser_IdxWithChatRoom(@Param("userIdx") Long userIdx);

    int countByChatRoom_Idx(Long chatRoomIdx);

    @Query("SELECT cp.chatRoom.idx, COUNT(cp) FROM ChatParticipant cp WHERE cp.chatRoom.idx IN :roomIds GROUP BY cp.chatRoom.idx")
    List<Object[]> countByChatRoomIdxIn(@Param("roomIds") List<Long> roomIds);
}
