package chainsawman.gesture.service;

import chainsawman.gesture.dto.chatRoom.request.ChatRoomInviteRespondRequest;
import chainsawman.gesture.dto.chatRoom.request.ChatRoomRequest;
import chainsawman.gesture.dto.chatRoom.request.ReadMarkRequest;
import chainsawman.gesture.dto.chatRoom.request.SendMessageRequest;
import chainsawman.gesture.entity.media.Media;
import chainsawman.gesture.exceptions.media.MediaNotFoundException;
import chainsawman.gesture.repository.media.MediaRepository;
import chainsawman.gesture.dto.chatRoom.response.*;
import chainsawman.gesture.dto.media.response.MediaUrlResponse;
import chainsawman.gesture.entity.chat.ChatMessage;
import chainsawman.gesture.entity.chat.ChatParticipant;
import chainsawman.gesture.entity.chat.ChatRoom;
import chainsawman.gesture.entity.chat.ChatRoomInvitation;
import chainsawman.gesture.entity.user.User;
import chainsawman.gesture.enums.InvitationStatus;
import chainsawman.gesture.enums.MessageType;
import chainsawman.gesture.exceptions.chat.ChatNotFoundException;
import chainsawman.gesture.exceptions.chat.ChatRoomInvitationAlreadyRespondedException;
import chainsawman.gesture.exceptions.chat.ChatRoomInvitationNotFoundException;
import chainsawman.gesture.exceptions.chat.ChatRoomNotParticipantException;
import chainsawman.gesture.exceptions.chat.ChatRoomNotFoundException;
import chainsawman.gesture.repository.chat.ChatMessageRepository;
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
import org.springframework.data.domain.PageRequest;
import org.springframework.test.util.ReflectionTestUtils;

import java.time.LocalDateTime;
import java.util.Collections;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class ChatRoomServiceTest {

    @Mock ChatRoomRepository chatRoomRepository;
    @Mock ChatParticipantRepository chatParticipantRepository;
    @Mock ChatRoomInvitationRepository chatRoomInvitationRepository;
    @Mock ChatMessageRepository chatMessageRepository;
    @Mock NotificationRepository notificationRepository;
    @Mock UserRepository userRepository;
    @Mock MediaRepository mediaRepository;
    @Mock MediaService mediaService;
    @Mock SecurityUtils securityUtils;

    @InjectMocks ChatRoomService chatRoomService;

    private User currentUser;
    private ChatRoom chatRoom;

    @BeforeEach
    void setUp() {
        currentUser = new User();
        ReflectionTestUtils.setField(currentUser, "idx", 1L);
        currentUser.setNickname("테스터");
        currentUser.setId("tester");
        given(securityUtils.getCurrentUser()).willReturn(currentUser);

        chatRoom = ChatRoom.builder().name("테스트방").build();
        ReflectionTestUtils.setField(chatRoom, "idx", 10L);
        ReflectionTestUtils.setField(chatRoom, "createdAt", LocalDateTime.of(2026, 1, 1, 0, 0));
        ReflectionTestUtils.setField(chatRoom, "updatedAt", LocalDateTime.of(2026, 1, 1, 0, 0));
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

    // ──────────────────────────────────────────────
    // getMyChatRooms
    // ──────────────────────────────────────────────

    @Test
    @DisplayName("내 채팅방 목록 조회 - 참여 중인 채팅방 목록 반환")
    void getMyChatRooms_returnsList() {
        ChatParticipant participant = buildParticipant(10L, chatRoom, currentUser);
        given(chatParticipantRepository.findByUser_IdxWithChatRoom(1L))
                .willReturn(List.of(participant));
        given(chatParticipantRepository.countByChatRoomIdxIn(List.of(10L)))
                .willReturn(Collections.singletonList(new Object[]{10L, 3L}));

        List<ChatRoomListResponse> result = chatRoomService.getMyChatRooms();

        assertThat(result).hasSize(1);
        assertThat(result.get(0).getChatRoomIdx()).isEqualTo(10L);
        assertThat(result.get(0).getName()).isEqualTo("테스트방");
        assertThat(result.get(0).getParticipantCount()).isEqualTo(3);
    }

    @Test
    @DisplayName("내 채팅방 목록 조회 - 참여 중인 채팅방 없으면 빈 리스트 반환")
    void getMyChatRooms_empty() {
        given(chatParticipantRepository.findByUser_IdxWithChatRoom(1L))
                .willReturn(List.of());

        List<ChatRoomListResponse> result = chatRoomService.getMyChatRooms();

        assertThat(result).isEmpty();
    }

    @Test
    @DisplayName("내 채팅방 목록 조회 - image_uuid 있으면 image_url 포함")
    void getMyChatRooms_withImage() {
        ChatRoom roomWithImage = ChatRoom.builder().name("이미지방").imageUuid("room-img-uuid").build();
        ReflectionTestUtils.setField(roomWithImage, "idx", 20L);
        ReflectionTestUtils.setField(roomWithImage, "createdAt", LocalDateTime.of(2026, 1, 1, 0, 0));

        ChatParticipant participant = buildParticipant(20L, roomWithImage, currentUser);
        given(chatParticipantRepository.findByUser_IdxWithChatRoom(1L))
                .willReturn(List.of(participant));
        given(chatParticipantRepository.countByChatRoomIdxIn(List.of(20L)))
                .willReturn(Collections.singletonList(new Object[]{20L, 1L}));
        given(mediaService.getMediaUrl("room-img-uuid"))
                .willReturn(MediaUrlResponse.builder().fileUrl("https://s3.example.com/room.png").build());

        List<ChatRoomListResponse> result = chatRoomService.getMyChatRooms();

        assertThat(result.get(0).getImageUrl()).isEqualTo("https://s3.example.com/room.png");
    }

    // ──────────────────────────────────────────────
    // getChatRoomDetail
    // ──────────────────────────────────────────────

    @Test
    @DisplayName("채팅방 상세 조회 - 참여자 목록과 함께 반환")
    void getChatRoomDetail_success() {
        User other = buildUser(2L, "다른사람", "other");

        given(chatParticipantRepository.findByChatRoom_IdxAndUser_Idx(10L, 1L))
                .willReturn(Optional.of(buildParticipant(1L, chatRoom, currentUser)));
        given(chatRoomRepository.findById(10L)).willReturn(Optional.of(chatRoom));
        given(chatParticipantRepository.findByChatRoom_Idx(10L))
                .willReturn(List.of(
                        buildParticipant(1L, chatRoom, currentUser),
                        buildParticipant(2L, chatRoom, other)
                ));
        given(mediaService.getProfileImageUrl(anyLong())).willReturn(Optional.empty());

        ChatRoomDetailResponse result = chatRoomService.getChatRoomDetail(10L);

        assertThat(result.getChatRoomIdx()).isEqualTo(10L);
        assertThat(result.getName()).isEqualTo("테스트방");
        assertThat(result.getParticipants()).hasSize(2);
        assertThat(result.getParticipants().get(0).getUserIdx()).isEqualTo(1L);
        assertThat(result.getParticipants().get(1).getUserIdx()).isEqualTo(2L);
    }

    @Test
    @DisplayName("채팅방 상세 조회 - 참여자가 아니면 ChatRoomNotParticipantException")
    void getChatRoomDetail_notParticipant() {
        given(chatParticipantRepository.findByChatRoom_IdxAndUser_Idx(10L, 1L)).willReturn(Optional.empty());

        assertThatThrownBy(() -> chatRoomService.getChatRoomDetail(10L))
                .isInstanceOf(ChatRoomNotParticipantException.class);
    }

    @Test
    @DisplayName("채팅방 상세 조회 - 채팅방이 없으면 ChatRoomNotFoundException")
    void getChatRoomDetail_roomNotFound() {
        given(chatParticipantRepository.findByChatRoom_IdxAndUser_Idx(10L, 1L))
                .willReturn(Optional.of(buildParticipant(1L, chatRoom, currentUser)));
        given(chatRoomRepository.findById(10L)).willReturn(Optional.empty());

        assertThatThrownBy(() -> chatRoomService.getChatRoomDetail(10L))
                .isInstanceOf(ChatRoomNotFoundException.class);
    }

    @Test
    @DisplayName("채팅방 상세 조회 - 프로필 이미지가 있으면 참여자에 포함")
    void getChatRoomDetail_participantWithProfileImage() {
        given(chatParticipantRepository.findByChatRoom_IdxAndUser_Idx(10L, 1L))
                .willReturn(Optional.of(buildParticipant(1L, chatRoom, currentUser)));
        given(chatRoomRepository.findById(10L)).willReturn(Optional.of(chatRoom));
        given(chatParticipantRepository.findByChatRoom_Idx(10L))
                .willReturn(List.of(buildParticipant(1L, chatRoom, currentUser)));
        given(mediaService.getProfileImageUrl(1L))
                .willReturn(Optional.of("https://s3.example.com/profile.jpg"));

        ChatRoomDetailResponse result = chatRoomService.getChatRoomDetail(10L);

        assertThat(result.getParticipants().get(0).getProfileImageUrl())
                .isEqualTo("https://s3.example.com/profile.jpg");
    }

    @Test
    @DisplayName("채팅방 상세 조회 - lastReadMessageIdx 포함")
    void getChatRoomDetail_includesLastReadMessageIdx() {
        ChatParticipant participant = buildParticipant(1L, chatRoom, currentUser);
        participant.setLastReadMessageIdx(42L);

        given(chatParticipantRepository.findByChatRoom_IdxAndUser_Idx(10L, 1L))
                .willReturn(Optional.of(buildParticipant(1L, chatRoom, currentUser)));
        given(chatRoomRepository.findById(10L)).willReturn(Optional.of(chatRoom));
        given(chatParticipantRepository.findByChatRoom_Idx(10L)).willReturn(List.of(participant));
        given(mediaService.getProfileImageUrl(anyLong())).willReturn(Optional.empty());

        ChatRoomDetailResponse result = chatRoomService.getChatRoomDetail(10L);

        assertThat(result.getParticipants().get(0).getLastReadMessageIdx()).isEqualTo(42L);
    }

    // ──────────────────────────────────────────────
    // leaveChatRoom
    // ──────────────────────────────────────────────

    @Test
    @DisplayName("채팅방 나가기 - 참여자 삭제 호출")
    void leaveChatRoom_success() {
        given(chatParticipantRepository.findByChatRoom_IdxAndUser_Idx(10L, 1L))
                .willReturn(Optional.of(buildParticipant(1L, chatRoom, currentUser)));

        chatRoomService.leaveChatRoom(10L);

        verify(chatParticipantRepository).deleteByUser_IdxAndChatRoom_Idx(1L, 10L);
    }

    @Test
    @DisplayName("채팅방 나가기 - 참여자가 아니면 ChatRoomNotParticipantException")
    void leaveChatRoom_notParticipant() {
        given(chatParticipantRepository.findByChatRoom_IdxAndUser_Idx(10L, 1L)).willReturn(Optional.empty());

        assertThatThrownBy(() -> chatRoomService.leaveChatRoom(10L))
                .isInstanceOf(ChatRoomNotParticipantException.class);

        verify(chatParticipantRepository, never()).deleteByUser_IdxAndChatRoom_Idx(anyLong(), anyLong());
    }

    // ──────────────────────────────────────────────
    // getMessages
    // ──────────────────────────────────────────────

    @Test
    @DisplayName("메시지 목록 조회 - cursor 없이 최신 메시지 반환")
    void getMessages_noCursor() {
        ChatMessage msg1 = buildMessage(2L, currentUser, chatRoom, "두번째");
        ChatMessage msg2 = buildMessage(1L, currentUser, chatRoom, "첫번째");

        given(chatParticipantRepository.findByChatRoom_IdxAndUser_Idx(10L, 1L))
                .willReturn(Optional.of(buildParticipant(1L, chatRoom, currentUser)));
        given(chatMessageRepository.findByChatRoom_IdxOrderByIdxDesc(eq(10L), any(PageRequest.class)))
                .willReturn(List.of(msg1, msg2));
        given(mediaService.getProfileImageUrl(anyLong())).willReturn(Optional.empty());

        ChatMessageListResponse result = chatRoomService.getMessages(10L, null, 20);

        assertThat(result.getMessages()).hasSize(2);
        assertThat(result.getMessages().get(0).getMessageIdx()).isEqualTo(2L);
        assertThat(result.getMessages().get(1).getMessageIdx()).isEqualTo(1L);
        assertThat(result.isHasNext()).isFalse();
        assertThat(result.getNextCursor()).isNull();
    }

    @Test
    @DisplayName("메시지 목록 조회 - cursor 있으면 cursor 이전 메시지 반환")
    void getMessages_withCursor() {
        ChatMessage msg = buildMessage(3L, currentUser, chatRoom, "세번째");

        given(chatParticipantRepository.findByChatRoom_IdxAndUser_Idx(10L, 1L))
                .willReturn(Optional.of(buildParticipant(1L, chatRoom, currentUser)));
        given(chatMessageRepository.findByChatRoom_IdxAndIdxLessThanOrderByIdxDesc(eq(10L), eq(5L), any(PageRequest.class)))
                .willReturn(List.of(msg));
        given(mediaService.getProfileImageUrl(anyLong())).willReturn(Optional.empty());

        ChatMessageListResponse result = chatRoomService.getMessages(10L, 5L, 20);

        assertThat(result.getMessages()).hasSize(1);
        assertThat(result.getMessages().get(0).getMessageIdx()).isEqualTo(3L);
        verify(chatMessageRepository, never()).findByChatRoom_IdxOrderByIdxDesc(anyLong(), any());
    }

    @Test
    @DisplayName("메시지 목록 조회 - size보다 많으면 hasNext=true, nextCursor 반환")
    void getMessages_hasNext() {
        List<ChatMessage> messages = List.of(
                buildMessage(5L, currentUser, chatRoom, "5번"),
                buildMessage(4L, currentUser, chatRoom, "4번"),
                buildMessage(3L, currentUser, chatRoom, "3번")  // size+1 번째 - 있음을 알림
        );

        given(chatParticipantRepository.findByChatRoom_IdxAndUser_Idx(10L, 1L))
                .willReturn(Optional.of(buildParticipant(1L, chatRoom, currentUser)));
        given(chatMessageRepository.findByChatRoom_IdxOrderByIdxDesc(eq(10L), any(PageRequest.class)))
                .willReturn(messages);
        given(mediaService.getProfileImageUrl(anyLong())).willReturn(Optional.empty());

        ChatMessageListResponse result = chatRoomService.getMessages(10L, null, 2);

        assertThat(result.getMessages()).hasSize(2);
        assertThat(result.isHasNext()).isTrue();
        assertThat(result.getNextCursor()).isEqualTo(4L);
    }

    @Test
    @DisplayName("메시지 목록 조회 - size 이하이면 hasNext=false")
    void getMessages_noMore() {
        List<ChatMessage> messages = List.of(
                buildMessage(2L, currentUser, chatRoom, "2번"),
                buildMessage(1L, currentUser, chatRoom, "1번")
        );

        given(chatParticipantRepository.findByChatRoom_IdxAndUser_Idx(10L, 1L))
                .willReturn(Optional.of(buildParticipant(1L, chatRoom, currentUser)));
        given(chatMessageRepository.findByChatRoom_IdxOrderByIdxDesc(eq(10L), any(PageRequest.class)))
                .willReturn(messages);
        given(mediaService.getProfileImageUrl(anyLong())).willReturn(Optional.empty());

        ChatMessageListResponse result = chatRoomService.getMessages(10L, null, 5);

        assertThat(result.isHasNext()).isFalse();
        assertThat(result.getNextCursor()).isNull();
    }

    @Test
    @DisplayName("메시지 목록 조회 - 참여자가 아니면 ChatRoomNotParticipantException")
    void getMessages_notParticipant() {
        given(chatParticipantRepository.findByChatRoom_IdxAndUser_Idx(10L, 1L)).willReturn(Optional.empty());

        assertThatThrownBy(() -> chatRoomService.getMessages(10L, null, 20))
                .isInstanceOf(ChatRoomNotParticipantException.class);
    }

    @Test
    @DisplayName("메시지 목록 조회 - 삭제된 메시지는 message와 file_url을 null로 반환")
    void getMessages_deletedMessage_nullContent() {
        ChatMessage deletedMsg = ChatMessage.builder()
                .sender(currentUser)
                .chatRoom(chatRoom)
                .message("삭제됨")
                .type(MessageType.TEXT)
                .build();
        ReflectionTestUtils.setField(deletedMsg, "idx", 1L);
        ReflectionTestUtils.setField(deletedMsg, "isDeleted", true);
        ReflectionTestUtils.setField(deletedMsg, "createdAt", LocalDateTime.now());

        given(chatParticipantRepository.findByChatRoom_IdxAndUser_Idx(10L, 1L))
                .willReturn(Optional.of(buildParticipant(1L, chatRoom, currentUser)));
        given(chatMessageRepository.findByChatRoom_IdxOrderByIdxDesc(eq(10L), any(PageRequest.class)))
                .willReturn(List.of(deletedMsg));
        given(mediaService.getProfileImageUrl(anyLong())).willReturn(Optional.empty());

        ChatMessageListResponse result = chatRoomService.getMessages(10L, null, 20);

        assertThat(result.getMessages().get(0).isDeleted()).isTrue();
        assertThat(result.getMessages().get(0).getMessage()).isNull();
        assertThat(result.getMessages().get(0).getFileUrl()).isNull();
    }

    // ──────────────────────────────────────────────
    // markAsRead
    // ──────────────────────────────────────────────

    @Test
    @DisplayName("읽음 처리 - lastReadMessageIdx 업데이트")
    void markAsRead_success() {
        ChatParticipant participant = buildParticipant(1L, chatRoom, currentUser);
        given(chatParticipantRepository.findByChatRoom_IdxAndUser_Idx(10L, 1L))
                .willReturn(Optional.of(participant));
        given(chatMessageRepository.existsByChatRoom_IdxAndIdx(10L, 99L)).willReturn(true);

        ReadMarkResponse result = chatRoomService.markAsRead(10L, new ReadMarkRequest(99L));

        assertThat(result.getChatRoomIdx()).isEqualTo(10L);
        assertThat(result.getLastReadMessageIdx()).isEqualTo(99L);
        assertThat(participant.getLastReadMessageIdx()).isEqualTo(99L);
    }

    @Test
    @DisplayName("읽음 처리 - 참여자가 아니면 ChatRoomNotParticipantException")
    void markAsRead_notParticipant() {
        given(chatParticipantRepository.findByChatRoom_IdxAndUser_Idx(10L, 1L))
                .willReturn(Optional.empty());

        assertThatThrownBy(() -> chatRoomService.markAsRead(10L, new ReadMarkRequest(99L)))
                .isInstanceOf(ChatRoomNotParticipantException.class);

        verify(chatParticipantRepository, never()).save(any());
    }

    @Test
    @DisplayName("읽음 처리 - 이전 lastReadMessageIdx를 새 값으로 덮어씀")
    void markAsRead_overwritesPreviousValue() {
        ChatParticipant participant = buildParticipant(1L, chatRoom, currentUser);
        participant.setLastReadMessageIdx(10L);

        given(chatParticipantRepository.findByChatRoom_IdxAndUser_Idx(10L, 1L))
                .willReturn(Optional.of(participant));
        given(chatMessageRepository.existsByChatRoom_IdxAndIdx(10L, 50L)).willReturn(true);

        chatRoomService.markAsRead(10L, new ReadMarkRequest(50L));

        assertThat(participant.getLastReadMessageIdx()).isEqualTo(50L);
    }

    @Test
    @DisplayName("읽음 처리 - 이 채팅방에 속하지 않는 메시지 idx면 ChatNotFoundException")
    void markAsRead_invalidMessageIdx() {
        ChatParticipant participant = buildParticipant(1L, chatRoom, currentUser);
        given(chatParticipantRepository.findByChatRoom_IdxAndUser_Idx(10L, 1L))
                .willReturn(Optional.of(participant));
        given(chatMessageRepository.existsByChatRoom_IdxAndIdx(10L, 9999L)).willReturn(false);

        assertThatThrownBy(() -> chatRoomService.markAsRead(10L, new ReadMarkRequest(9999L)))
                .isInstanceOf(ChatNotFoundException.class);
    }

    // ──────────────────────────────────────────────
    // sendMessage
    // ──────────────────────────────────────────────

    @Test
    @DisplayName("메시지 전송 - TEXT 타입 정상 저장")
    void sendMessage_text_success() {
        ChatMessage saved = buildMessage(100L, currentUser, chatRoom, "안녕하세요");

        given(chatParticipantRepository.findByChatRoom_IdxAndUser_Idx(10L, 1L))
                .willReturn(Optional.of(buildParticipant(1L, chatRoom, currentUser)));
        given(chatRoomRepository.findById(10L)).willReturn(Optional.of(chatRoom));
        given(chatMessageRepository.save(any(ChatMessage.class))).willReturn(saved);
        given(mediaService.getProfileImageUrl(1L)).willReturn(Optional.empty());

        ChatMessageResponse result = chatRoomService.sendMessage(10L,
                new SendMessageRequest(MessageType.TEXT, "안녕하세요", null));

        assertThat(result.getMessageIdx()).isEqualTo(100L);
        assertThat(result.getMessage()).isEqualTo("안녕하세요");
        assertThat(result.getFileUrl()).isNull();
    }

    @Test
    @DisplayName("메시지 전송 - FILE 타입 정상 저장")
    void sendMessage_file_success() {
        Media media = Media.builder().uuid("file-uuid").name("test.png").build();
        ReflectionTestUtils.setField(media, "idx", 1L);

        ChatMessage saved = ChatMessage.builder()
                .sender(currentUser).chatRoom(chatRoom).type(MessageType.FILE).file(media).build();
        ReflectionTestUtils.setField(saved, "idx", 101L);
        ReflectionTestUtils.setField(saved, "createdAt", LocalDateTime.of(2026, 1, 1, 0, 0));

        given(chatParticipantRepository.findByChatRoom_IdxAndUser_Idx(10L, 1L))
                .willReturn(Optional.of(buildParticipant(1L, chatRoom, currentUser)));
        given(chatRoomRepository.findById(10L)).willReturn(Optional.of(chatRoom));
        given(mediaRepository.findByUuid("file-uuid")).willReturn(Optional.of(media));
        given(chatMessageRepository.save(any(ChatMessage.class))).willReturn(saved);
        given(mediaService.getProfileImageUrl(1L)).willReturn(Optional.empty());
        given(mediaService.getMediaUrl("file-uuid"))
                .willReturn(MediaUrlResponse.builder().fileUrl("https://s3.example.com/test.png").build());

        ChatMessageResponse result = chatRoomService.sendMessage(10L,
                new SendMessageRequest(MessageType.FILE, null, "file-uuid"));

        assertThat(result.getMessageIdx()).isEqualTo(101L);
        assertThat(result.getFileUrl()).isEqualTo("https://s3.example.com/test.png");
    }

    @Test
    @DisplayName("메시지 전송 - 참여자가 아니면 ChatRoomNotParticipantException")
    void sendMessage_notParticipant() {
        given(chatParticipantRepository.findByChatRoom_IdxAndUser_Idx(10L, 1L))
                .willReturn(Optional.empty());

        assertThatThrownBy(() -> chatRoomService.sendMessage(10L,
                new SendMessageRequest(MessageType.TEXT, "안녕", null)))
                .isInstanceOf(ChatRoomNotParticipantException.class);
    }

    @Test
    @DisplayName("메시지 전송 - 존재하지 않는 file_uuid면 MediaNotFoundException")
    void sendMessage_fileNotFound() {
        given(chatParticipantRepository.findByChatRoom_IdxAndUser_Idx(10L, 1L))
                .willReturn(Optional.of(buildParticipant(1L, chatRoom, currentUser)));
        given(chatRoomRepository.findById(10L)).willReturn(Optional.of(chatRoom));
        given(mediaRepository.findByUuid("bad-uuid")).willReturn(Optional.empty());

        assertThatThrownBy(() -> chatRoomService.sendMessage(10L,
                new SendMessageRequest(MessageType.FILE, null, "bad-uuid")))
                .isInstanceOf(MediaNotFoundException.class);
    }

    // ──────────────────────────────────────────────
    // helpers
    // ──────────────────────────────────────────────

    private User buildUser(Long idx, String nickname, String id) {
        User user = new User();
        ReflectionTestUtils.setField(user, "idx", idx);
        user.setNickname(nickname);
        user.setId(id);
        return user;
    }

    private ChatParticipant buildParticipant(Long idx, ChatRoom chatRoom, User user) {
        ChatParticipant participant = ChatParticipant.builder()
                .chatRoom(chatRoom)
                .user(user)
                .build();
        ReflectionTestUtils.setField(participant, "idx", idx);
        return participant;
    }

    private ChatMessage buildMessage(Long idx, User sender, ChatRoom chatRoom, String text) {
        ChatMessage message = ChatMessage.builder()
                .sender(sender)
                .chatRoom(chatRoom)
                .message(text)
                .type(MessageType.TEXT)
                .build();
        ReflectionTestUtils.setField(message, "idx", idx);
        ReflectionTestUtils.setField(message, "createdAt", LocalDateTime.of(2026, 1, 1, 0, 0));
        return message;
    }
}
