package chainsawman.gesture.service;

import chainsawman.gesture.entity.call.Call;
import chainsawman.gesture.entity.call.CallParticipant;
import chainsawman.gesture.entity.friend.FriendInvite;
import chainsawman.gesture.entity.meeting.Meeting;
import chainsawman.gesture.entity.room.Room;
import chainsawman.gesture.entity.room.RoomMember;
import chainsawman.gesture.entity.user.User;
import chainsawman.gesture.enums.MeetingStatus;
import chainsawman.gesture.enums.RoomRole;
import chainsawman.gesture.repository.call.CallParticipantRepository;
import chainsawman.gesture.repository.call.CallRepository;
import chainsawman.gesture.repository.friend.FriendInviteRepository;
import chainsawman.gesture.repository.meeting.MeetingRepository;
import chainsawman.gesture.repository.room.RoomMemberRepository;
import chainsawman.gesture.repository.room.RoomRepository;
import chainsawman.gesture.security.SecurityUtils;
import jakarta.persistence.EntityManager;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.context.annotation.Import;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.test.context.bean.override.mockito.MockitoBean;

import java.time.LocalDateTime;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

// 실제 DB(H2) 외래키 제약조건으로 방 삭제를 검증
@DataJpaTest
@Import(RoomService.class)
class RoomDeleteIntegrationTest {

    @MockitoBean MediaService mediaService;
    @MockitoBean SecurityUtils securityUtils;
    @MockitoBean PasswordEncoder passwordEncoder;

    @Autowired RoomService roomService;
    @Autowired EntityManager em;
    @Autowired RoomRepository roomRepository;
    @Autowired RoomMemberRepository roomMemberRepository;
    @Autowired CallRepository callRepository;
    @Autowired CallParticipantRepository callParticipantRepository;
    @Autowired MeetingRepository meetingRepository;
    @Autowired FriendInviteRepository friendInviteRepository;

    @Test
    @DisplayName("방 삭제 - 통화/통화참여자/회의록/친구초대/멤버가 있어도 외래키 오류 없이 삭제")
    void deleteRoomWithRelations_removes_all_children() {
        User host = persistUser("host");
        User friend = persistUser("friend");

        Room room = Room.builder().host(host).title("방").maxParticipant(5).build();
        em.persist(room);
        em.persist(RoomMember.builder().room(room).user(host).role(RoomRole.HOST).build());

        Call call = Call.builder().room(room).startedBy(host).endedAt(LocalDateTime.now()).build();
        em.persist(call);
        em.persist(CallParticipant.builder().call(call).user(host).build());

        em.persist(Meeting.builder()
                .call(call).room(room).createdBy(host)
                .status(MeetingStatus.ENDED)
                .participants(List.of("host", "friend"))
                .conclusion(List.of("결론"))
                .build());
        em.persist(FriendInvite.builder().sender(host).receiver(friend).room(room).build());

        em.flush();
        em.clear();

        Long roomIdx = room.getIdx();
        roomService.deleteRoomWithRelations(roomRepository.findById(roomIdx).orElseThrow());
        em.flush();

        assertThat(roomRepository.findById(roomIdx)).isEmpty();
        assertThat(roomMemberRepository.count()).isZero();
        assertThat(callRepository.count()).isZero();
        assertThat(callParticipantRepository.count()).isZero();
        assertThat(meetingRepository.count()).isZero();
        assertThat(friendInviteRepository.count()).isZero();
    }

    private User persistUser(String name) {
        User user = User.builder()
                .email(name + "@test.com")
                .nickname(name)
                .id(name)
                .build();
        em.persist(user);
        return user;
    }
}
