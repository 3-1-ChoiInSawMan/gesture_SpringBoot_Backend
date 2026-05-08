package chainsawman.gesture.service;

import chainsawman.gesture.dto.media.response.MediaUrlResponse;
import chainsawman.gesture.dto.room.request.RoomPatchRequest;
import chainsawman.gesture.dto.room.request.RoomRequest;
import chainsawman.gesture.dto.room.response.RoomPatchResponse;
import chainsawman.gesture.dto.room.response.RoomResponse;
import chainsawman.gesture.entity.chat.ChatParticipant;
import chainsawman.gesture.entity.chat.ChatRoom;
import chainsawman.gesture.entity.room.Room;
import chainsawman.gesture.entity.room.RoomMember;
import chainsawman.gesture.entity.user.User;
import chainsawman.gesture.exceptions.media.MediaNotFoundException;
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
}
