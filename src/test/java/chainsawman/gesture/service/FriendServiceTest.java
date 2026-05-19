package chainsawman.gesture.service;

import chainsawman.gesture.dto.friend.request.FriendInviteRequest;
import chainsawman.gesture.dto.friend.request.FriendRequestRespondRequest;
import chainsawman.gesture.dto.friend.response.*;
import chainsawman.gesture.entity.friend.FriendInvite;
import chainsawman.gesture.entity.friend.Friendship;
import chainsawman.gesture.entity.room.Room;
import chainsawman.gesture.entity.user.User;
import chainsawman.gesture.enums.FriendshipStatus;
import chainsawman.gesture.enums.InvitationStatus;
import chainsawman.gesture.exceptions.friend.DuplicateFriendInviteException;
import chainsawman.gesture.exceptions.room.NotRoomHostException;
import chainsawman.gesture.exceptions.friend.FriendRequestNotFoundException;
import chainsawman.gesture.exceptions.friend.FriendshipNotFoundException;
import chainsawman.gesture.exceptions.friend.InvalidFriendRequestStatusException;
import chainsawman.gesture.exceptions.room.RoomNotFoundException;
import chainsawman.gesture.exceptions.user.UserNotFoundException;
import chainsawman.gesture.repository.friend.FriendInviteRepository;
import chainsawman.gesture.repository.friend.FriendshipRepository;
import chainsawman.gesture.repository.room.RoomRepository;
import chainsawman.gesture.repository.user.UserRepository;
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
import static org.mockito.Mockito.verify;

@ExtendWith(MockitoExtension.class)
class FriendServiceTest {

    @Mock FriendshipRepository friendshipRepository;
    @Mock FriendInviteRepository friendInviteRepository;
    @Mock UserRepository userRepository;
    @Mock RoomRepository roomRepository;
    @Mock SecurityUtils securityUtils;

    @InjectMocks FriendService friendService;

    private User sender;
    private User receiver;
    private Room room;

    @BeforeEach
    void setUp() {
        sender = new User();
        ReflectionTestUtils.setField(sender, "idx", 1L);
        sender.setNickname("윤정");
        sender.setId("yoonjeong");

        receiver = new User();
        ReflectionTestUtils.setField(receiver, "idx", 2L);
        receiver.setNickname("친구");
        receiver.setId("friend");

        room = Room.builder().host(sender).title("테스트방").maxParticipant(5).build();
        ReflectionTestUtils.setField(room, "idx", 55L);
    }

    // ─── invite ───────────────────────────────────

    @Test
    @DisplayName("친구 통화방 초대 - 성공")
    void invite_success() {
        FriendInviteRequest request = new FriendInviteRequest(2L, 55L);

        given(securityUtils.getCurrentUser()).willReturn(sender);
        given(userRepository.findByIdxAndIsDeactivatedFalse(2L)).willReturn(Optional.of(receiver));
        given(roomRepository.findById(55L)).willReturn(Optional.of(room));
        given(friendInviteRepository.save(any(FriendInvite.class))).willAnswer(inv -> {
            FriendInvite invite = inv.getArgument(0);
            ReflectionTestUtils.setField(invite, "idx", 10L);
            ReflectionTestUtils.setField(invite, "createdAt", LocalDateTime.of(2026, 4, 8, 13, 32, 57));
            return invite;
        });

        FriendInviteResponse response = friendService.invite(request);

        assertThat(response.getInviteIdx()).isEqualTo(10L);
        assertThat(response.getSenderIdx()).isEqualTo(1L);
        assertThat(response.getReceiverIdx()).isEqualTo(2L);
        assertThat(response.getRoomIdx()).isEqualTo(55L);
        assertThat(response.getMessage()).isEqualTo("윤정님이 통화방에 초대했습니다.");
        assertThat(response.getCreatedAt()).isEqualTo(LocalDateTime.of(2026, 4, 8, 13, 32, 57));
        verify(friendInviteRepository).save(any(FriendInvite.class));
    }

    @Test
    @DisplayName("친구 통화방 초대 - 수신자가 존재하지 않으면 UserNotFoundException")
    void invite_receiver_not_found() {
        FriendInviteRequest request = new FriendInviteRequest(99L, 55L);

        given(securityUtils.getCurrentUser()).willReturn(sender);
        given(userRepository.findByIdxAndIsDeactivatedFalse(99L)).willReturn(Optional.empty());

        assertThatThrownBy(() -> friendService.invite(request))
                .isInstanceOf(UserNotFoundException.class);
    }

    @Test
    @DisplayName("친구 통화방 초대 - sender가 host가 아니면 NotRoomHostException")
    void invite_not_host() {
        FriendInviteRequest request = new FriendInviteRequest(2L, 55L);

        given(securityUtils.getCurrentUser()).willReturn(receiver); // receiver는 host가 아님
        given(userRepository.findByIdxAndIsDeactivatedFalse(2L)).willReturn(Optional.of(receiver));
        given(roomRepository.findById(55L)).willReturn(Optional.of(room));

        assertThatThrownBy(() -> friendService.invite(request))
                .isInstanceOf(NotRoomHostException.class);
    }

    @Test
    @DisplayName("친구 통화방 초대 - 이미 PENDING 초대가 있으면 DuplicateFriendInviteException")
    void invite_duplicate_pending() {
        FriendInviteRequest request = new FriendInviteRequest(2L, 55L);

        given(securityUtils.getCurrentUser()).willReturn(sender);
        given(userRepository.findByIdxAndIsDeactivatedFalse(2L)).willReturn(Optional.of(receiver));
        given(roomRepository.findById(55L)).willReturn(Optional.of(room));
        given(friendInviteRepository.existsBySender_IdxAndReceiver_IdxAndRoom_IdxAndStatus(
                1L, 2L, 55L, InvitationStatus.PENDING)).willReturn(true);

        assertThatThrownBy(() -> friendService.invite(request))
                .isInstanceOf(DuplicateFriendInviteException.class);
    }

    @Test
    @DisplayName("친구 통화방 초대 - 방이 존재하지 않으면 RoomNotFoundException")
    void invite_room_not_found() {
        FriendInviteRequest request = new FriendInviteRequest(2L, 999L);

        given(securityUtils.getCurrentUser()).willReturn(sender);
        given(userRepository.findByIdxAndIsDeactivatedFalse(2L)).willReturn(Optional.of(receiver));
        given(roomRepository.findById(999L)).willReturn(Optional.empty());

        assertThatThrownBy(() -> friendService.invite(request))
                .isInstanceOf(RoomNotFoundException.class);
    }

    // ─── postFriend ───────────────────────────────

    @Test
    @DisplayName("친구 요청 발송 - 성공")
    void postFriend_success() {
        given(securityUtils.getCurrentUser()).willReturn(sender);
        given(userRepository.findByIdxAndIsDeactivatedFalse(2L)).willReturn(Optional.of(receiver));
        given(friendshipRepository.save(any(Friendship.class))).willAnswer(inv -> {
            Friendship f = inv.getArgument(0);
            ReflectionTestUtils.setField(f, "idx", 1L);
            ReflectionTestUtils.setField(f, "requestAt", LocalDateTime.now());
            return f;
        });

        FriendRequestSendResponse response = friendService.postFriend(2L);

        assertThat(response.getFriendRequestIdx()).isEqualTo(1L);
        assertThat(response.getRequesterIdx()).isEqualTo(1L);
        assertThat(response.getReceiverIdx()).isEqualTo(2L);
        assertThat(response.getStatus()).isEqualTo("PENDING");
        verify(friendshipRepository).save(any(Friendship.class));
    }

    @Test
    @DisplayName("친구 요청 발송 - 수신자가 존재하지 않으면 UserNotFoundException")
    void postFriend_receiver_not_found() {
        given(securityUtils.getCurrentUser()).willReturn(sender);
        given(userRepository.findByIdxAndIsDeactivatedFalse(99L)).willReturn(Optional.empty());

        assertThatThrownBy(() -> friendService.postFriend(99L))
                .isInstanceOf(UserNotFoundException.class);
    }

    // ─── getFriendRequests ────────────────────────

    @Test
    @DisplayName("친구 요청 목록 조회 - PENDING 상태 목록 반환")
    void getFriendRequests_returns_pending_list() {
        Friendship friendship = buildFriendship(1L, sender, receiver, FriendshipStatus.PENDING);

        given(securityUtils.getCurrentUser()).willReturn(receiver);
        given(friendshipRepository.findAllByFriend_IdxAndStatus(2L, FriendshipStatus.PENDING))
                .willReturn(List.of(friendship));

        List<FriendRequestListResponse> result = friendService.getFriendRequests();

        assertThat(result).hasSize(1);
        assertThat(result.get(0).getFriendRequestIdx()).isEqualTo(1L);
        assertThat(result.get(0).getRequesterIdx()).isEqualTo(1L);
        assertThat(result.get(0).getStatus()).isEqualTo("PENDING");
    }

    // ─── respondToFriendRequest ───────────────────

    @Test
    @DisplayName("친구 요청 수락 - ACCEPTED로 상태 변경")
    void respondToFriendRequest_accept() {
        Friendship friendship = buildFriendship(1L, sender, receiver, FriendshipStatus.PENDING);
        FriendRequestRespondRequest request = new FriendRequestRespondRequest("ACCEPTED");

        given(securityUtils.getCurrentUser()).willReturn(receiver);
        given(friendshipRepository.findByIdxAndFriend_Idx(1L, 2L)).willReturn(Optional.of(friendship));
        given(friendshipRepository.save(any(Friendship.class))).willAnswer(inv -> inv.getArgument(0));

        FriendRequestRespondResponse response = friendService.respondToFriendRequest(1L, request);

        assertThat(response.getStatus()).isEqualTo("ACCEPTED");
        assertThat(friendship.getStatus()).isEqualTo(FriendshipStatus.ACCEPTED);
    }

    @Test
    @DisplayName("친구 요청 거절 - REJECTED로 상태 변경")
    void respondToFriendRequest_reject() {
        Friendship friendship = buildFriendship(1L, sender, receiver, FriendshipStatus.PENDING);
        FriendRequestRespondRequest request = new FriendRequestRespondRequest("REJECTED");

        given(securityUtils.getCurrentUser()).willReturn(receiver);
        given(friendshipRepository.findByIdxAndFriend_Idx(1L, 2L)).willReturn(Optional.of(friendship));
        given(friendshipRepository.save(any(Friendship.class))).willAnswer(inv -> inv.getArgument(0));

        FriendRequestRespondResponse response = friendService.respondToFriendRequest(1L, request);

        assertThat(response.getStatus()).isEqualTo("REJECTED");
        assertThat(friendship.getStatus()).isEqualTo(FriendshipStatus.REJECTED);
    }

    @Test
    @DisplayName("친구 요청 응답 - 잘못된 상태값이면 InvalidFriendRequestStatusException")
    void respondToFriendRequest_invalid_status() {
        Friendship friendship = buildFriendship(1L, sender, receiver, FriendshipStatus.PENDING);
        FriendRequestRespondRequest request = new FriendRequestRespondRequest("INVALID");

        given(securityUtils.getCurrentUser()).willReturn(receiver);
        given(friendshipRepository.findByIdxAndFriend_Idx(1L, 2L)).willReturn(Optional.of(friendship));

        assertThatThrownBy(() -> friendService.respondToFriendRequest(1L, request))
                .isInstanceOf(InvalidFriendRequestStatusException.class);
    }

    @Test
    @DisplayName("친구 요청 응답 - PENDING으로 변경 시도 시 InvalidFriendRequestStatusException")
    void respondToFriendRequest_cannot_set_pending() {
        Friendship friendship = buildFriendship(1L, sender, receiver, FriendshipStatus.PENDING);
        FriendRequestRespondRequest request = new FriendRequestRespondRequest("PENDING");

        given(securityUtils.getCurrentUser()).willReturn(receiver);
        given(friendshipRepository.findByIdxAndFriend_Idx(1L, 2L)).willReturn(Optional.of(friendship));

        assertThatThrownBy(() -> friendService.respondToFriendRequest(1L, request))
                .isInstanceOf(InvalidFriendRequestStatusException.class);
    }

    @Test
    @DisplayName("친구 요청 응답 - 요청이 없으면 FriendRequestNotFoundException")
    void respondToFriendRequest_not_found() {
        FriendRequestRespondRequest request = new FriendRequestRespondRequest("ACCEPTED");

        given(securityUtils.getCurrentUser()).willReturn(receiver);
        given(friendshipRepository.findByIdxAndFriend_Idx(99L, 2L)).willReturn(Optional.empty());

        assertThatThrownBy(() -> friendService.respondToFriendRequest(99L, request))
                .isInstanceOf(FriendRequestNotFoundException.class);
    }

    // ─── getFriendList ────────────────────────────

    @Test
    @DisplayName("친구 목록 조회 - 양방향 ACCEPTED 목록 반환")
    void getFriendList_returns_bidirectional_list() {
        User anotherUser = new User();
        ReflectionTestUtils.setField(anotherUser, "idx", 3L);
        anotherUser.setNickname("세번째");
        anotherUser.setId("third");

        Friendship sentFriendship = buildFriendship(1L, sender, receiver, FriendshipStatus.ACCEPTED);
        Friendship receivedFriendship = buildFriendship(2L, anotherUser, sender, FriendshipStatus.ACCEPTED);

        given(securityUtils.getCurrentUser()).willReturn(sender);
        given(friendshipRepository.findAllByUser_IdxAndStatus(1L, FriendshipStatus.ACCEPTED))
                .willReturn(List.of(sentFriendship));
        given(friendshipRepository.findAllByFriend_IdxAndStatus(1L, FriendshipStatus.ACCEPTED))
                .willReturn(List.of(receivedFriendship));

        List<FriendListResponse> result = friendService.getFriendList();

        assertThat(result).hasSize(2);
    }

    // ─── getFriendCount ───────────────────────────

    @Test
    @DisplayName("친구 수 조회 - 양방향 합산 반환")
    void getFriendCount_returns_total_count() {
        given(securityUtils.getCurrentUser()).willReturn(sender);
        given(friendshipRepository.countByUser_IdxAndStatus(1L, FriendshipStatus.ACCEPTED)).willReturn(3L);
        given(friendshipRepository.countByFriend_IdxAndStatus(1L, FriendshipStatus.ACCEPTED)).willReturn(2L);

        FriendCountResponse response = friendService.getFriendCount();

        assertThat(response.getCount()).isEqualTo(5L);
    }

    // ─── deleteFriend ─────────────────────────────

    @Test
    @DisplayName("친구 삭제 - 성공")
    void deleteFriend_success() {
        Friendship friendship = buildFriendship(1L, sender, receiver, FriendshipStatus.ACCEPTED);

        given(securityUtils.getCurrentUser()).willReturn(sender);
        given(friendshipRepository.findByUser_IdxAndFriend_IdxAndStatus(1L, 2L, FriendshipStatus.ACCEPTED))
                .willReturn(Optional.of(friendship));

        FriendDeleteResponse response = friendService.deleteFriend(2L);

        assertThat(response.isDeleted()).isTrue();
        assertThat(response.getUserIdx()).isEqualTo(1L);
        assertThat(response.getTargetUserIdx()).isEqualTo(2L);
        verify(friendshipRepository).delete(friendship);
    }

    @Test
    @DisplayName("친구 삭제 - 친구 관계가 없으면 FriendshipNotFoundException")
    void deleteFriend_not_found() {
        given(securityUtils.getCurrentUser()).willReturn(sender);
        given(friendshipRepository.findByUser_IdxAndFriend_IdxAndStatus(1L, 99L, FriendshipStatus.ACCEPTED))
                .willReturn(Optional.empty());
        given(friendshipRepository.findByUser_IdxAndFriend_IdxAndStatus(99L, 1L, FriendshipStatus.ACCEPTED))
                .willReturn(Optional.empty());

        assertThatThrownBy(() -> friendService.deleteFriend(99L))
                .isInstanceOf(FriendshipNotFoundException.class);
    }

    // ─── helpers ──────────────────────────────────

    private Friendship buildFriendship(Long idx, User user, User friend, FriendshipStatus status) {
        Friendship friendship = Friendship.builder()
                .user(user)
                .friend(friend)
                .status(status)
                .build();
        ReflectionTestUtils.setField(friendship, "idx", idx);
        ReflectionTestUtils.setField(friendship, "requestAt", LocalDateTime.now());
        return friendship;
    }
}
