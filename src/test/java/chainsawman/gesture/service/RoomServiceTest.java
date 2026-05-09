package chainsawman.gesture.service;

import chainsawman.gesture.dto.media.response.MediaUrlResponse;
import chainsawman.gesture.dto.room.request.RoomPatchRequest;
import chainsawman.gesture.dto.room.request.RoomRequest;
import chainsawman.gesture.dto.room.response.RoomLeaveResponse;
import chainsawman.gesture.dto.room.response.RoomPatchResponse;
import chainsawman.gesture.dto.room.response.RoomResponse;
import chainsawman.gesture.entity.chat.ChatParticipant;
import chainsawman.gesture.entity.chat.ChatRoom;
import chainsawman.gesture.entity.room.Room;
import chainsawman.gesture.entity.room.RoomMember;
import chainsawman.gesture.entity.user.User;
import chainsawman.gesture.enums.RoomRole;
import chainsawman.gesture.exceptions.media.MediaNotFoundException;
import chainsawman.gesture.exceptions.room.RoomMemberNotFoundException;
import chainsawman.gesture.exceptions.room.RoomNotFoundException;
import chainsawman.gesture.repository.chat.ChatParticipantRepository;
import chainsawman.gesture.repository.chat.ChatRoomRepository;
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
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.test.util.ReflectionTestUtils;

import java.time.LocalDateTime;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;

@ExtendWith(MockitoExtension.class)
class RoomServiceTest {

    @Mock RoomRepository roomRepository;
    @Mock RoomMemberRepository roomMemberRepository;
    @Mock ChatRoomRepository chatRoomRepository;
    @Mock ChatParticipantRepository chatParticipantRepository;
    @Mock MediaService mediaService;
    @Mock SecurityUtils securityUtils;
    @Mock PasswordEncoder passwordEncoder;

    @InjectMocks RoomService roomService;

    private User user;

    @BeforeEach
    void setUp() {
        user = new User();
        ReflectionTestUtils.setField(user, "idx", 1L);
        given(securityUtils.getCurrentUser()).willReturn(user);
    }

    // ─── createRoom ───────────────────────────────

    @Test
    @DisplayName("방 생성 - thumbnail_uuid 없으면 thumbnailUrl null")
    void createRoom_no_thumbnail() {
        RoomRequest request = new RoomRequest("제목", "STUDY", 5, true, null, null);

        given(chatRoomRepository.save(any(ChatRoom.class))).willAnswer(inv -> {
            ChatRoom cr = inv.getArgument(0);
            ReflectionTestUtils.setField(cr, "idx", 1L);
            return cr;
        });
        given(chatParticipantRepository.save(any(ChatParticipant.class))).willAnswer(inv -> inv.getArgument(0));
        given(roomRepository.save(any(Room.class))).willAnswer(inv -> {
            Room r = inv.getArgument(0);
            ReflectionTestUtils.setField(r, "idx", 10L);
            ReflectionTestUtils.setField(r, "createdAt", LocalDateTime.now());
            return r;
        });
        given(roomMemberRepository.save(any(RoomMember.class))).willAnswer(inv -> inv.getArgument(0));

        RoomResponse response = roomService.createRoom(request);

        assertThat(response.getThumbnailUrl()).isNull();
        verify(mediaService, never()).getMediaUrl(any());
    }

    @Test
    @DisplayName("방 생성 - thumbnail_uuid 있으면 S3 URL 저장")
    void createRoom_with_thumbnail() {
        RoomRequest request = new RoomRequest("제목", "STUDY", 5, true, null, "thumb-uuid");

        given(mediaService.getMediaUrl("thumb-uuid"))
                .willReturn(MediaUrlResponse.builder().fileUrl("https://s3.example.com/thumb.jpg").build());
        given(chatRoomRepository.save(any(ChatRoom.class))).willAnswer(inv -> {
            ChatRoom cr = inv.getArgument(0);
            ReflectionTestUtils.setField(cr, "idx", 1L);
            return cr;
        });
        given(chatParticipantRepository.save(any(ChatParticipant.class))).willAnswer(inv -> inv.getArgument(0));
        given(roomRepository.save(any(Room.class))).willAnswer(inv -> {
            Room r = inv.getArgument(0);
            ReflectionTestUtils.setField(r, "idx", 10L);
            ReflectionTestUtils.setField(r, "createdAt", LocalDateTime.now());
            return r;
        });
        given(roomMemberRepository.save(any(RoomMember.class))).willAnswer(inv -> inv.getArgument(0));

        RoomResponse response = roomService.createRoom(request);

        assertThat(response.getThumbnailUrl()).isEqualTo("https://s3.example.com/thumb.jpg");
    }

    @Test
    @DisplayName("방 생성 - 잘못된 thumbnail_uuid면 MediaNotFoundException, 방 저장 안됨")
    void createRoom_invalid_thumbnail() {
        RoomRequest request = new RoomRequest("제목", "STUDY", 5, true, null, "bad-uuid");

        given(mediaService.getMediaUrl("bad-uuid")).willThrow(new MediaNotFoundException());

        assertThatThrownBy(() -> roomService.createRoom(request))
                .isInstanceOf(MediaNotFoundException.class);

        verify(roomRepository, never()).save(any());
    }

    // ─── patchRoom ────────────────────────────────

    @Test
    @DisplayName("방 수정 - thumbnail_uuid 있으면 thumbnailUrl 업데이트")
    void patchRoom_with_thumbnail() {
        Room room = new Room();
        room.setHost(user);
        room.setTitle("기존제목");
        ReflectionTestUtils.setField(room, "idx", 1L);
        ReflectionTestUtils.setField(room, "createdAt", LocalDateTime.now());
        ReflectionTestUtils.setField(room, "updatedAt", LocalDateTime.now());

        RoomPatchRequest request = new RoomPatchRequest(null, null, 0, null, null, "new-thumb");

        given(roomRepository.findById(1L)).willReturn(Optional.of(room));
        given(roomMemberRepository.countByRoom_Idx(1L)).willReturn(2);
        given(mediaService.getMediaUrl("new-thumb"))
                .willReturn(MediaUrlResponse.builder().fileUrl("https://s3.example.com/new.jpg").build());
        given(roomRepository.save(any())).willAnswer(inv -> inv.getArgument(0));

        RoomPatchResponse response = roomService.patchRoom(1L, request);

        assertThat(response.getThumbnailUrl()).isEqualTo("https://s3.example.com/new.jpg");
    }

    @Test
    @DisplayName("방 수정 - 잘못된 thumbnail_uuid면 MediaNotFoundException")
    void patchRoom_invalid_thumbnail() {
        Room room = new Room();
        room.setHost(user);
        ReflectionTestUtils.setField(room, "idx", 1L);

        RoomPatchRequest request = new RoomPatchRequest(null, null, 0, null, null, "bad-uuid");

        given(roomRepository.findById(1L)).willReturn(Optional.of(room));
        given(roomMemberRepository.countByRoom_Idx(1L)).willReturn(1);
        given(mediaService.getMediaUrl("bad-uuid")).willThrow(new MediaNotFoundException());

        assertThatThrownBy(() -> roomService.patchRoom(1L, request))
                .isInstanceOf(MediaNotFoundException.class);
    }

    @Test
    @DisplayName("방 수정 - 존재하지 않는 방이면 RoomNotFoundException")
    void patchRoom_room_not_found() {
        given(roomRepository.findById(99L)).willReturn(Optional.empty());

        assertThatThrownBy(() -> roomService.patchRoom(99L, new RoomPatchRequest(null, null, 0, null, null, null)))
                .isInstanceOf(RoomNotFoundException.class);
    }

    // ─── leaveRoom ────────────────────────────────

    @Test
    @DisplayName("통화방 나가기 - 일반 멤버가 나가면 deleted false, newHostIdx null")
    void leaveRoom_member() {
        Room room = buildRoom(1L, user, null);
        RoomMember member = buildMember(user, RoomRole.MEMBER);

        given(roomRepository.findById(1L)).willReturn(Optional.of(room));
        given(roomMemberRepository.findByRoom_IdxAndUser_Idx(1L, 1L)).willReturn(Optional.of(member));

        RoomLeaveResponse response = roomService.leaveRoom(1L);

        assertThat(response.isDeleted()).isFalse();
        assertThat(response.getNewHostIdx()).isNull();
        verify(roomMemberRepository).deleteByRoom_IdxAndUser_Idx(1L, 1L);
        verify(roomRepository, never()).delete(any());
    }

    @Test
    @DisplayName("통화방 나가기 - 방장이 나가고 다음 멤버 있으면 방장 위임")
    void leaveRoom_host_delegates() {
        User nextUser = new User();
        ReflectionTestUtils.setField(nextUser, "idx", 2L);

        Room room = buildRoom(1L, user, null);
        RoomMember hostMember = buildMember(user, RoomRole.HOST);
        RoomMember nextMember = buildMember(nextUser, RoomRole.MEMBER);

        given(roomRepository.findById(1L)).willReturn(Optional.of(room));
        given(roomMemberRepository.findByRoom_IdxAndUser_Idx(1L, 1L)).willReturn(Optional.of(hostMember));
        given(roomMemberRepository.findTopByRoom_IdxAndUser_IdxNotOrderByCreatedAtAsc(1L, 1L))
                .willReturn(Optional.of(nextMember));
        given(roomMemberRepository.save(any())).willAnswer(inv -> inv.getArgument(0));
        given(roomRepository.save(any())).willAnswer(inv -> inv.getArgument(0));

        RoomLeaveResponse response = roomService.leaveRoom(1L);

        assertThat(response.isDeleted()).isFalse();
        assertThat(response.getNewHostIdx()).isEqualTo(2L);
        assertThat(nextMember.getRole()).isEqualTo(RoomRole.HOST);
        verify(roomMemberRepository).deleteByRoom_IdxAndUser_Idx(1L, 1L);
        verify(roomRepository, never()).delete(any());
    }

    @Test
    @DisplayName("통화방 나가기 - 방장이 마지막 멤버이면 방 삭제")
    void leaveRoom_host_last_member() {
        Room room = buildRoom(1L, user, null);
        RoomMember hostMember = buildMember(user, RoomRole.HOST);

        given(roomRepository.findById(1L)).willReturn(Optional.of(room));
        given(roomMemberRepository.findByRoom_IdxAndUser_Idx(1L, 1L)).willReturn(Optional.of(hostMember));
        given(roomMemberRepository.findTopByRoom_IdxAndUser_IdxNotOrderByCreatedAtAsc(1L, 1L))
                .willReturn(Optional.empty());

        RoomLeaveResponse response = roomService.leaveRoom(1L);

        assertThat(response.isDeleted()).isTrue();
        verify(roomMemberRepository).deleteAllByRoom_Idx(1L);
        verify(roomRepository).delete(room);
    }

    @Test
    @DisplayName("통화방 나가기 - 존재하지 않는 방이면 RoomNotFoundException")
    void leaveRoom_room_not_found() {
        given(roomRepository.findById(99L)).willReturn(Optional.empty());

        assertThatThrownBy(() -> roomService.leaveRoom(99L))
                .isInstanceOf(RoomNotFoundException.class);
    }

    @Test
    @DisplayName("통화방 나가기 - 참여하지 않은 방이면 RoomMemberNotFoundException")
    void leaveRoom_not_a_member() {
        Room room = buildRoom(1L, user, null);

        given(roomRepository.findById(1L)).willReturn(Optional.of(room));
        given(roomMemberRepository.findByRoom_IdxAndUser_Idx(1L, 1L)).willReturn(Optional.empty());

        assertThatThrownBy(() -> roomService.leaveRoom(1L))
                .isInstanceOf(RoomMemberNotFoundException.class);
    }

    @Test
    @DisplayName("통화방 나가기 - 채팅방 연결 시 채팅방에서도 퇴장")
    void leaveRoom_also_leaves_chatroom() {
        ChatRoom chatRoom = ChatRoom.builder().name("채팅방").build();
        ReflectionTestUtils.setField(chatRoom, "idx", 10L);

        Room room = buildRoom(1L, user, chatRoom);
        RoomMember member = buildMember(user, RoomRole.MEMBER);

        given(roomRepository.findById(1L)).willReturn(Optional.of(room));
        given(roomMemberRepository.findByRoom_IdxAndUser_Idx(1L, 1L)).willReturn(Optional.of(member));

        roomService.leaveRoom(1L);

        verify(chatParticipantRepository).deleteByUser_IdxAndChatRoom_Idx(1L, 10L);
    }

    private Room buildRoom(Long idx, User host, ChatRoom chatRoom) {
        Room room = Room.builder().host(host).title("테스트방").maxParticipant(5).chatRoom(chatRoom).build();
        ReflectionTestUtils.setField(room, "idx", idx);
        return room;
    }

    private RoomMember buildMember(User user, RoomRole role) {
        RoomMember member = RoomMember.builder().user(user).role(role).build();
        ReflectionTestUtils.setField(member, "idx", 100L);
        return member;
    }
}
