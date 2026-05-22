package chainsawman.gesture.service;

import chainsawman.gesture.dto.call.response.CallJoinResponse;
import chainsawman.gesture.dto.call.response.CallLeaveResponse;
import chainsawman.gesture.dto.call.response.CallParticipantsResponse;
import chainsawman.gesture.entity.call.Call;
import chainsawman.gesture.entity.call.CallParticipant;
import chainsawman.gesture.entity.room.Room;
import chainsawman.gesture.entity.user.User;
import chainsawman.gesture.exceptions.call.CallAlreadyJoinedException;
import chainsawman.gesture.exceptions.call.CallParticipantNotFoundException;
import chainsawman.gesture.exceptions.call.NoActiveCallException;
import chainsawman.gesture.exceptions.room.NotRoomMemberException;
import chainsawman.gesture.exceptions.room.RoomNotFoundException;
import chainsawman.gesture.repository.call.CallParticipantRepository;
import chainsawman.gesture.repository.call.CallRepository;
import chainsawman.gesture.repository.room.RoomMemberRepository;
import chainsawman.gesture.repository.room.RoomRepository;
import chainsawman.gesture.security.SecurityUtils;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.util.ReflectionTestUtils;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;

@ExtendWith(MockitoExtension.class)
class CallServiceTest {

    @Mock CallRepository callRepository;
    @Mock CallParticipantRepository callParticipantRepository;
    @Mock RoomRepository roomRepository;
    @Mock RoomMemberRepository roomMemberRepository;
    @Mock SecurityUtils securityUtils;

    @InjectMocks CallService callService;

    private User user;
    private Room room;

    @BeforeEach
    void setUp() {
        user = new User();
        ReflectionTestUtils.setField(user, "idx", 1L);
        user.setNickname("테스터");

        room = Room.builder().host(user).title("테스트방").maxParticipant(5).build();
        ReflectionTestUtils.setField(room, "idx", 10L);
    }

    // ─── joinCall ─────────────────────────────────

    @Test
    @DisplayName("통화 참여 - 활성 통화 없으면 새 세션 생성 후 참여")
    void joinCall_creates_new_session_when_no_active_call() {
        Call newCall = buildCall(100L, user);

        given(securityUtils.getCurrentUser()).willReturn(user);
        given(roomRepository.findById(10L)).willReturn(Optional.of(room));
        given(roomMemberRepository.existsByRoom_IdxAndUser_Idx(10L, 1L)).willReturn(true);
        given(callRepository.findByRoom_IdxAndEndedAtIsNull(10L)).willReturn(Optional.empty());
        given(callRepository.save(any(Call.class))).willReturn(newCall);
        given(callParticipantRepository.existsByUser_IdxAndLeftAtIsNull(1L)).willReturn(false);
        given(callParticipantRepository.save(any(CallParticipant.class))).willAnswer(inv -> {
            CallParticipant p = inv.getArgument(0);
            ReflectionTestUtils.setField(p, "joinedAt", LocalDateTime.now());
            return p;
        });
        given(callParticipantRepository.countByCall_IdxAndLeftAtIsNull(100L)).willReturn(1);

        CallJoinResponse response = callService.joinCall(10L);

        assertThat(response.getCallIdx()).isEqualTo(100L);
        assertThat(response.getRoomIdx()).isEqualTo(10L);
        assertThat(response.getUserIdx()).isEqualTo(1L);
        assertThat(response.getCurrentParticipant()).isEqualTo(1);
        assertThat(response.getMaxParticipant()).isEqualTo(5);
        verify(callRepository).save(any(Call.class));
    }

    @Test
    @DisplayName("통화 참여 - 이미 활성 통화 있으면 기존 세션에 참여")
    void joinCall_joins_existing_session() {
        Call existingCall = buildCall(100L, user);

        given(securityUtils.getCurrentUser()).willReturn(user);
        given(roomRepository.findById(10L)).willReturn(Optional.of(room));
        given(roomMemberRepository.existsByRoom_IdxAndUser_Idx(10L, 1L)).willReturn(true);
        given(callRepository.findByRoom_IdxAndEndedAtIsNull(10L)).willReturn(Optional.of(existingCall));
        given(callParticipantRepository.existsByUser_IdxAndLeftAtIsNull(1L)).willReturn(false);
        given(callParticipantRepository.save(any(CallParticipant.class))).willAnswer(inv -> {
            CallParticipant p = inv.getArgument(0);
            ReflectionTestUtils.setField(p, "joinedAt", LocalDateTime.now());
            return p;
        });
        given(callParticipantRepository.countByCall_IdxAndLeftAtIsNull(100L)).willReturn(2);

        CallJoinResponse response = callService.joinCall(10L);

        assertThat(response.getCallIdx()).isEqualTo(100L);
        assertThat(response.getCurrentParticipant()).isEqualTo(2);
        // 기존 세션 재사용 → callRepository.save 호출 안됨
        verify(callRepository, never()).save(any(Call.class));
    }

    @Test
    @DisplayName("통화 참여 - 존재하지 않는 방이면 RoomNotFoundException")
    void joinCall_room_not_found() {
        given(securityUtils.getCurrentUser()).willReturn(user);
        given(roomRepository.findById(99L)).willReturn(Optional.empty());

        assertThatThrownBy(() -> callService.joinCall(99L))
                .isInstanceOf(RoomNotFoundException.class);
    }

    @Test
    @DisplayName("통화 참여 - 방 멤버가 아니면 NotRoomMemberException")
    void joinCall_not_a_room_member() {
        given(securityUtils.getCurrentUser()).willReturn(user);
        given(roomRepository.findById(10L)).willReturn(Optional.of(room));
        given(roomMemberRepository.existsByRoom_IdxAndUser_Idx(10L, 1L)).willReturn(false);

        assertThatThrownBy(() -> callService.joinCall(10L))
                .isInstanceOf(NotRoomMemberException.class);
    }

    @Test
    @DisplayName("통화 참여 - 이미 통화 참여 중이면 CallAlreadyJoinedException")
    void joinCall_already_joined() {
        Call existingCall = buildCall(100L, user);

        given(securityUtils.getCurrentUser()).willReturn(user);
        given(roomRepository.findById(10L)).willReturn(Optional.of(room));
        given(roomMemberRepository.existsByRoom_IdxAndUser_Idx(10L, 1L)).willReturn(true);
        given(callRepository.findByRoom_IdxAndEndedAtIsNull(10L)).willReturn(Optional.of(existingCall));
        given(callParticipantRepository.existsByUser_IdxAndLeftAtIsNull(1L)).willReturn(true);

        assertThatThrownBy(() -> callService.joinCall(10L))
                .isInstanceOf(CallAlreadyJoinedException.class);
    }

    // ─── leaveCall ────────────────────────────────

    @Test
    @DisplayName("통화 나가기 - 남은 참여자 있으면 세션 유지")
    void leaveCall_session_continues_when_others_remain() {
        Call call = buildCall(100L, user);
        CallParticipant participant = buildParticipant(call, user);

        given(securityUtils.getCurrentUser()).willReturn(user);
        given(roomRepository.findById(10L)).willReturn(Optional.of(room));
        given(callRepository.findByRoom_IdxAndEndedAtIsNull(10L)).willReturn(Optional.of(call));
        given(callParticipantRepository.findByCall_IdxAndUser_IdxAndLeftAtIsNull(100L, 1L))
                .willReturn(Optional.of(participant));
        given(callParticipantRepository.save(any(CallParticipant.class))).willAnswer(inv -> inv.getArgument(0));
        given(callParticipantRepository.countByCall_IdxAndLeftAtIsNull(100L)).willReturn(1);

        CallLeaveResponse response = callService.leaveCall(10L);

        assertThat(response.getCallIdx()).isEqualTo(100L);
        assertThat(response.getUserIdx()).isEqualTo(1L);
        assertThat(response.isCallEnded()).isFalse();
        assertThat(response.getCurrentParticipant()).isEqualTo(1);
        assertThat(participant.getLeftAt()).isNotNull();
        verify(callRepository, never()).save(any(Call.class));
    }

    @Test
    @DisplayName("통화 나가기 - 마지막 참여자 나가면 세션 종료")
    void leaveCall_ends_session_when_last_participant_leaves() {
        Call call = buildCall(100L, user);
        CallParticipant participant = buildParticipant(call, user);

        given(securityUtils.getCurrentUser()).willReturn(user);
        given(roomRepository.findById(10L)).willReturn(Optional.of(room));
        given(callRepository.findByRoom_IdxAndEndedAtIsNull(10L)).willReturn(Optional.of(call));
        given(callParticipantRepository.findByCall_IdxAndUser_IdxAndLeftAtIsNull(100L, 1L))
                .willReturn(Optional.of(participant));
        given(callParticipantRepository.save(any(CallParticipant.class))).willAnswer(inv -> inv.getArgument(0));
        given(callParticipantRepository.countByCall_IdxAndLeftAtIsNull(100L)).willReturn(0);
        given(callRepository.save(any(Call.class))).willAnswer(inv -> inv.getArgument(0));

        CallLeaveResponse response = callService.leaveCall(10L);

        assertThat(response.isCallEnded()).isTrue();
        assertThat(response.getCurrentParticipant()).isEqualTo(0);
        assertThat(call.getEndedAt()).isNotNull();
        verify(callRepository).save(call);
    }

    @Test
    @DisplayName("통화 나가기 - 존재하지 않는 방이면 RoomNotFoundException")
    void leaveCall_room_not_found() {
        given(securityUtils.getCurrentUser()).willReturn(user);
        given(roomRepository.findById(99L)).willReturn(Optional.empty());

        assertThatThrownBy(() -> callService.leaveCall(99L))
                .isInstanceOf(RoomNotFoundException.class);
    }

    @Test
    @DisplayName("통화 나가기 - 활성 통화 없으면 NoActiveCallException")
    void leaveCall_no_active_call() {
        given(securityUtils.getCurrentUser()).willReturn(user);
        given(roomRepository.findById(10L)).willReturn(Optional.of(room));
        given(callRepository.findByRoom_IdxAndEndedAtIsNull(10L)).willReturn(Optional.empty());

        assertThatThrownBy(() -> callService.leaveCall(10L))
                .isInstanceOf(NoActiveCallException.class);
    }

    @Test
    @DisplayName("통화 나가기 - 통화 참여 중이 아니면 CallParticipantNotFoundException")
    void leaveCall_participant_not_found() {
        Call call = buildCall(100L, user);

        given(securityUtils.getCurrentUser()).willReturn(user);
        given(roomRepository.findById(10L)).willReturn(Optional.of(room));
        given(callRepository.findByRoom_IdxAndEndedAtIsNull(10L)).willReturn(Optional.of(call));
        given(callParticipantRepository.findByCall_IdxAndUser_IdxAndLeftAtIsNull(100L, 1L))
                .willReturn(Optional.empty());

        assertThatThrownBy(() -> callService.leaveCall(10L))
                .isInstanceOf(CallParticipantNotFoundException.class);
    }

    // ─── getParticipants ──────────────────────────

    @Test
    @DisplayName("참가자 조회 - 목록과 호스트 여부 반환")
    void getParticipants_returns_list_with_host_flag() {
        User other = new User();
        ReflectionTestUtils.setField(other, "idx", 2L);
        other.setNickname("다른유저");

        Call call = buildCall(100L, user); // user가 호스트
        CallParticipant hostParticipant = buildParticipant(call, user);
        CallParticipant otherParticipant = buildParticipant(call, other);

        given(roomRepository.findById(10L)).willReturn(Optional.of(room));
        given(callRepository.findByRoom_IdxAndEndedAtIsNull(10L)).willReturn(Optional.of(call));
        given(callParticipantRepository.findByCall_IdxAndLeftAtIsNullOrderByJoinedAtAsc(100L))
                .willReturn(List.of(hostParticipant, otherParticipant));

        CallParticipantsResponse response = callService.getParticipants(10L);

        assertThat(response.getCallIdx()).isEqualTo(100L);
        assertThat(response.getRoomIdx()).isEqualTo(10L);
        assertThat(response.getCurrentParticipant()).isEqualTo(2);
        assertThat(response.getParticipants()).hasSize(2);
        assertThat(response.getParticipants().get(0).isHost()).isTrue();
        assertThat(response.getParticipants().get(1).isHost()).isFalse();
    }

    @Test
    @DisplayName("참가자 조회 - 존재하지 않는 방이면 RoomNotFoundException")
    void getParticipants_room_not_found() {
        given(roomRepository.findById(99L)).willReturn(Optional.empty());

        assertThatThrownBy(() -> callService.getParticipants(99L))
                .isInstanceOf(RoomNotFoundException.class);
    }

    @Test
    @DisplayName("참가자 조회 - 활성 통화 없으면 NoActiveCallException")
    void getParticipants_no_active_call() {
        given(roomRepository.findById(10L)).willReturn(Optional.of(room));
        given(callRepository.findByRoom_IdxAndEndedAtIsNull(10L)).willReturn(Optional.empty());

        assertThatThrownBy(() -> callService.getParticipants(10L))
                .isInstanceOf(NoActiveCallException.class);
    }

    // ─── helpers ──────────────────────────────────

    private Call buildCall(Long idx, User startedBy) {
        Call call = Call.builder().room(room).startedBy(startedBy).build();
        ReflectionTestUtils.setField(call, "idx", idx);
        return call;
    }

    private CallParticipant buildParticipant(Call call, User participantUser) {
        CallParticipant participant = CallParticipant.builder().call(call).user(participantUser).build();
        ReflectionTestUtils.setField(participant, "joinedAt", LocalDateTime.now());
        return participant;
    }
}
