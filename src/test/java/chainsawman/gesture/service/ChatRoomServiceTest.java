package chainsawman.gesture.service;

import chainsawman.gesture.dto.chatRoom.request.ChatRoomInviteRespondRequest;
import chainsawman.gesture.dto.chatRoom.request.ChatRoomRequest;
import chainsawman.gesture.dto.chatRoom.response.ChatRoomInviteRespondResponse;
import chainsawman.gesture.dto.chatRoom.response.ChatRoomResponse;
import chainsawman.gesture.dto.media.response.MediaUrlResponse;
import chainsawman.gesture.entity.chat.ChatParticipant;
import chainsawman.gesture.entity.chat.ChatRoom;
import chainsawman.gesture.entity.chat.ChatRoomInvitation;
import chainsawman.gesture.entity.user.User;
import chainsawman.gesture.enums.InvitationStatus;
import chainsawman.gesture.exceptions.chat.ChatRoomInvitationAlreadyRespondedException;
import chainsawman.gesture.exceptions.chat.ChatRoomInvitationNotFoundException;
import chainsawman.gesture.repository.chat.ChatParticipantRepository;
import chainsawman.gesture.repository.chat.ChatRoomInvitationRepository;
import chainsawman.gesture.repository.chat.ChatRoomRepository;
import chainsawman.gesture.repository.notification.NotificationRepository;
import chainsawman.gesture.repository.user.UserRepository;
import chainsawman.gesture.security.SecurityUtils;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.util.ReflectionTestUtils;

import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class ChatRoomServiceTest {

    @Mock ChatRoomRepository chatRoomRepository;
    @Mock ChatParticipantRepository chatParticipantRepository;
    @Mock ChatRoomInvitationRepository chatRoomInvitationRepository;
    @Mock NotificationRepository notificationRepository;
    @Mock UserRepository userRepository;
    @Mock MediaService mediaService;
    @Mock SecurityUtils securityUtils;

    @InjectMocks ChatRoomService chatRoomService;

    private User currentUser;
    private ChatRoom chatRoom;

    @BeforeEach
    void setUp() {
        currentUser = new User();
        ReflectionTestUtils.setField(currentUser, "idx", 1L);
        given(securityUtils.getCurrentUser()).willReturn(currentUser);

        chatRoom = ChatRoom.builder().name("테스트방").build();
        ReflectionTestUtils.setField(chatRoom, "idx", 10L);
    }

    private void stubCreateChatRoom() {
        given(chatRoomRepository.save(any(ChatRoom.class))).willReturn(chatRoom);
        given(chatParticipantRepository.save(any(ChatParticipant.class))).willAnswer(inv -> inv.getArgument(0));
    }

    // ──────────────────────────────────────────────
    // createChatRoom
    // ──────────────────────────────────────────────

    @Test
    @DisplayName("채팅방 생성 - 초대 없이 본인만 참여자로 등록")
    void createChatRoom_noInvitees() {
        stubCreateChatRoom();
        ChatRoomRequest request = new ChatRoomRequest("테스트방", null, null);

        ChatRoomResponse response = chatRoomService.createChatRoom(request);

        assertThat(response.getChatRoomIdx()).isEqualTo(10L);
        assertThat(response.getParticipants()).hasSize(1);
        verify(chatRoomInvitationRepository, never()).save(any());
    }

    @Test
    @DisplayName("채팅방 생성 - 초대 대상자는 ChatParticipant가 아닌 초대장으로 등록")
    void createChatRoom_withInvitees() {
        stubCreateChatRoom();
        User invitee = new User();
        ReflectionTestUtils.setField(invitee, "idx", 2L);

        ChatRoomRequest request = new ChatRoomRequest("테스트방", null, List.of(2L));
        given(userRepository.findAllById(List.of(2L))).willReturn(List.of(invitee));
        given(chatRoomInvitationRepository.save(any(ChatRoomInvitation.class))).willAnswer(inv -> inv.getArgument(0));

        ChatRoomResponse response = chatRoomService.createChatRoom(request);

        assertThat(response.getParticipants()).hasSize(1);
        verify(chatRoomInvitationRepository, times(1)).save(any(ChatRoomInvitation.class));
        verify(chatParticipantRepository, times(1)).save(any(ChatParticipant.class));
    }

    @Test
    @DisplayName("채팅방 생성 - participant_ids에 본인 포함 시 본인은 초대 제외")
    void createChatRoom_selfInviteIgnored() {
        stubCreateChatRoom();
        ChatRoomRequest request = new ChatRoomRequest("테스트방", null, List.of(1L));
        given(userRepository.findAllById(List.of(1L))).willReturn(List.of(currentUser));

        chatRoomService.createChatRoom(request);

        verify(chatRoomInvitationRepository, never()).save(any());
    }

    @Test
    @DisplayName("채팅방 생성 - image_uuid 있으면 image_url 반환")
    void createChatRoom_withImage() {
        stubCreateChatRoom();
        ChatRoomRequest request = new ChatRoomRequest("테스트방", "img-uuid", null);
        given(mediaService.getMediaUrl("img-uuid"))
                .willReturn(MediaUrlResponse.builder().fileUrl("https://s3.example.com/img.png").build());

        ChatRoomResponse response = chatRoomService.createChatRoom(request);

        assertThat(response.getImageUrl()).isEqualTo("https://s3.example.com/img.png");
    }

    @Test
    @DisplayName("채팅방 생성 - image_uuid 없으면 image_url null")
    void createChatRoom_noImage() {
        stubCreateChatRoom();
        ChatRoomRequest request = new ChatRoomRequest("테스트방", null, null);

        ChatRoomResponse response = chatRoomService.createChatRoom(request);

        assertThat(response.getImageUrl()).isNull();
        verify(mediaService, never()).getMediaUrl(any());
    }

    // ──────────────────────────────────────────────
    // respondToInvitation
    // ──────────────────────────────────────────────

    @Test
    @DisplayName("초대 수락 - ChatParticipant 등록 및 상태 ACCEPTED")
    void respondToInvitation_accept() {
        ChatRoomInvitation invitation = ChatRoomInvitation.builder()
                .chatRoom(chatRoom)
                .inviter(new User())
                .invitee(currentUser)
                .build();
        ReflectionTestUtils.setField(invitation, "idx", 100L);

        given(chatRoomInvitationRepository.findByIdxAndInvitee_Idx(100L, 1L))
                .willReturn(Optional.of(invitation));
        given(chatRoomInvitationRepository.save(any())).willAnswer(inv -> inv.getArgument(0));

        ChatRoomInviteRespondResponse response =
                chatRoomService.respondToInvitation(100L, new ChatRoomInviteRespondRequest(true));

        assertThat(response.getStatus()).isEqualTo("ACCEPTED");
        assertThat(invitation.getStatus()).isEqualTo(InvitationStatus.ACCEPTED);
        verify(chatParticipantRepository).save(any(ChatParticipant.class));
    }

    @Test
    @DisplayName("초대 거절 - ChatParticipant 미등록 및 상태 DECLINED")
    void respondToInvitation_decline() {
        ChatRoomInvitation invitation = ChatRoomInvitation.builder()
                .chatRoom(chatRoom)
                .inviter(new User())
                .invitee(currentUser)
                .build();
        ReflectionTestUtils.setField(invitation, "idx", 100L);

        given(chatRoomInvitationRepository.findByIdxAndInvitee_Idx(100L, 1L))
                .willReturn(Optional.of(invitation));
        given(chatRoomInvitationRepository.save(any())).willAnswer(inv -> inv.getArgument(0));

        ChatRoomInviteRespondResponse response =
                chatRoomService.respondToInvitation(100L, new ChatRoomInviteRespondRequest(false));

        assertThat(response.getStatus()).isEqualTo("DECLINED");
        assertThat(invitation.getStatus()).isEqualTo(InvitationStatus.DECLINED);
        verify(chatParticipantRepository, never()).save(any());
    }

    @Test
    @DisplayName("초대 수락/거절 - 존재하지 않는 초대이면 ChatRoomInvitationNotFoundException")
    void respondToInvitation_notFound() {
        given(chatRoomInvitationRepository.findByIdxAndInvitee_Idx(999L, 1L))
                .willReturn(Optional.empty());

        assertThatThrownBy(() ->
                chatRoomService.respondToInvitation(999L, new ChatRoomInviteRespondRequest(true)))
                .isInstanceOf(ChatRoomInvitationNotFoundException.class);

        verify(chatParticipantRepository, never()).save(any());
    }

    @Test
    @DisplayName("초대 수락/거절 - 이미 수락된 초대이면 ChatRoomInvitationAlreadyRespondedException")
    void respondToInvitation_alreadyAccepted() {
        ChatRoomInvitation invitation = ChatRoomInvitation.builder()
                .chatRoom(chatRoom)
                .inviter(new User())
                .invitee(currentUser)
                .build();
        invitation.accept();
        ReflectionTestUtils.setField(invitation, "idx", 100L);

        given(chatRoomInvitationRepository.findByIdxAndInvitee_Idx(100L, 1L))
                .willReturn(Optional.of(invitation));

        assertThatThrownBy(() ->
                chatRoomService.respondToInvitation(100L, new ChatRoomInviteRespondRequest(true)))
                .isInstanceOf(ChatRoomInvitationAlreadyRespondedException.class);
    }

    @Test
    @DisplayName("초대 수락/거절 - 이미 거절된 초대이면 ChatRoomInvitationAlreadyRespondedException")
    void respondToInvitation_alreadyDeclined() {
        ChatRoomInvitation invitation = ChatRoomInvitation.builder()
                .chatRoom(chatRoom)
                .inviter(new User())
                .invitee(currentUser)
                .build();
        invitation.decline();
        ReflectionTestUtils.setField(invitation, "idx", 100L);

        given(chatRoomInvitationRepository.findByIdxAndInvitee_Idx(100L, 1L))
                .willReturn(Optional.of(invitation));

        assertThatThrownBy(() ->
                chatRoomService.respondToInvitation(100L, new ChatRoomInviteRespondRequest(false)))
                .isInstanceOf(ChatRoomInvitationAlreadyRespondedException.class);
    }

    @Test
    @DisplayName("초대 수락 - 수락 시 올바른 chatRoom과 user로 ChatParticipant 생성")
    void respondToInvitation_accept_correctParticipant() {
        ChatRoomInvitation invitation = ChatRoomInvitation.builder()
                .chatRoom(chatRoom)
                .inviter(new User())
                .invitee(currentUser)
                .build();
        ReflectionTestUtils.setField(invitation, "idx", 100L);

        given(chatRoomInvitationRepository.findByIdxAndInvitee_Idx(100L, 1L))
                .willReturn(Optional.of(invitation));
        given(chatRoomInvitationRepository.save(any())).willAnswer(inv -> inv.getArgument(0));

        ArgumentCaptor<ChatParticipant> captor = ArgumentCaptor.forClass(ChatParticipant.class);
        given(chatParticipantRepository.save(captor.capture())).willAnswer(inv -> inv.getArgument(0));

        chatRoomService.respondToInvitation(100L, new ChatRoomInviteRespondRequest(true));

        assertThat(captor.getValue().getChatRoom().getIdx()).isEqualTo(10L);
        assertThat(captor.getValue().getUser()).isEqualTo(currentUser);
    }
}
