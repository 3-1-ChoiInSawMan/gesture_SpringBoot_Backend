package chainsawman.gesture.service;

import chainsawman.gesture.dto.chatRoom.request.ChatRoomInviteRespondRequest;
import chainsawman.gesture.dto.chatRoom.request.ChatRoomRequest;
import chainsawman.gesture.dto.chatRoom.request.ReadMarkRequest;
import chainsawman.gesture.dto.chatRoom.request.SendMessageRequest;
import chainsawman.gesture.entity.media.Media;
import chainsawman.gesture.dto.chatRoom.response.*;
import chainsawman.gesture.entity.chat.ChatMessage;
import chainsawman.gesture.entity.chat.ChatParticipant;
import chainsawman.gesture.entity.chat.ChatRoom;
import chainsawman.gesture.entity.chat.ChatRoomInvitation;
import chainsawman.gesture.entity.notification.Notification;
import chainsawman.gesture.entity.user.User;
import chainsawman.gesture.enums.InvitationStatus;
import chainsawman.gesture.enums.NotificationType;
import chainsawman.gesture.exceptions.chat.ChatNotFoundException;
import chainsawman.gesture.exceptions.chat.ChatRoomInvitationAlreadyRespondedException;
import chainsawman.gesture.exceptions.chat.ChatRoomInvitationNotFoundException;
import chainsawman.gesture.exceptions.chat.ChatRoomNotParticipantException;
import chainsawman.gesture.exceptions.chat.ChatRoomNotFoundException;
import chainsawman.gesture.exceptions.media.MediaNotFoundException;
import chainsawman.gesture.repository.chat.ChatMessageRepository;
import chainsawman.gesture.repository.chat.ChatParticipantRepository;
import chainsawman.gesture.repository.chat.ChatRoomInvitationRepository;
import chainsawman.gesture.repository.chat.ChatRoomRepository;
import chainsawman.gesture.repository.media.MediaRepository;
import chainsawman.gesture.repository.notification.NotificationRepository;
import chainsawman.gesture.repository.user.UserRepository;
import chainsawman.gesture.security.SecurityUtils;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class ChatRoomService {

    private final ChatRoomRepository chatRoomRepository;
    private final ChatParticipantRepository chatParticipantRepository;
    private final ChatRoomInvitationRepository chatRoomInvitationRepository;
    private final ChatMessageRepository chatMessageRepository;
    private final NotificationRepository notificationRepository;
    private final UserRepository userRepository;
    private final MediaRepository mediaRepository;
    private final MediaService mediaService;
    private final SecurityUtils securityUtils;

    @Transactional
    public ChatRoomResponse createChatRoom(ChatRoomRequest request) {
        User currentUser = securityUtils.getCurrentUser();

        ChatRoom chatRoom = chatRoomRepository.save(ChatRoom.builder()
                .name(request.getName())
                .imageUuid(request.getImageUuid())
                .build());

        chatParticipantRepository.save(ChatParticipant.builder()
                .chatRoom(chatRoom)
                .user(currentUser)
                .build());

        if (request.getParticipantIds() != null && !request.getParticipantIds().isEmpty()) {
            List<User> invitees = userRepository.findAllById(request.getParticipantIds()).stream()
                    .filter(u -> !u.getIdx().equals(currentUser.getIdx()))
                    .toList();

            invitees.forEach(invitee -> {
                ChatRoomInvitation invitation = chatRoomInvitationRepository.save(ChatRoomInvitation.builder()
                        .chatRoom(chatRoom)
                        .inviter(currentUser)
                        .invitee(invitee)
                        .build());

                notificationRepository.save(Notification.builder()
                        .user(invitee)
                        .actor(currentUser)
                        .type(NotificationType.CHAT_ROOM_INVITATION)
                        .targetId(String.valueOf(invitation.getIdx()))
                        .content(currentUser.getNickname() + "(@" + currentUser.getId() + ")님이 채팅방에 초대했습니다.")
                        .isRead(false)
                        .build());
            });
        }

        String imageUrl = request.getImageUuid() != null
                ? mediaService.getMediaUrl(request.getImageUuid()).getFileUrl()
                : null;

        return ChatRoomResponse.from(chatRoom, List.of(currentUser), imageUrl);
    }

    @Transactional
    public ChatRoomInviteRespondResponse respondToInvitation(Long invitationIdx, ChatRoomInviteRespondRequest request) {
        User currentUser = securityUtils.getCurrentUser();

        ChatRoomInvitation invitation = chatRoomInvitationRepository
                .findByIdxAndInvitee_Idx(invitationIdx, currentUser.getIdx())
                .orElseThrow(ChatRoomInvitationNotFoundException::new);

        if (invitation.getStatus() != InvitationStatus.PENDING) {
            throw new ChatRoomInvitationAlreadyRespondedException();
        }

        if (request.isAccept()) {
            invitation.accept();
            chatParticipantRepository.save(ChatParticipant.builder()
                    .chatRoom(invitation.getChatRoom())
                    .user(currentUser)
                    .build());
        } else {
            invitation.decline();
        }

        chatRoomInvitationRepository.save(invitation);

        return ChatRoomInviteRespondResponse.from(invitation);
    }

    @Transactional(readOnly = true)
    public List<ChatRoomListResponse> getMyChatRooms() {
        User currentUser = securityUtils.getCurrentUser();

        List<ChatParticipant> myParticipations = chatParticipantRepository
                .findByUser_IdxWithChatRoom(currentUser.getIdx());

        if (myParticipations.isEmpty()) {
            return List.of();
        }

        List<Long> roomIds = myParticipations.stream()
                .map(cp -> cp.getChatRoom().getIdx())
                .toList();

        Map<Long, Long> countMap = chatParticipantRepository.countByChatRoomIdxIn(roomIds).stream()
                .collect(Collectors.toMap(row -> (Long) row[0], row -> (Long) row[1]));

        return myParticipations.stream()
                .map(cp -> {
                    ChatRoom chatRoom = cp.getChatRoom();
                    int participantCount = countMap.getOrDefault(chatRoom.getIdx(), 0L).intValue();
                    String imageUrl = chatRoom.getImageUuid() != null
                            ? mediaService.getMediaUrl(chatRoom.getImageUuid()).getFileUrl()
                            : null;
                    return ChatRoomListResponse.from(chatRoom, participantCount, imageUrl);
                })
                .toList();
    }

    @Transactional(readOnly = true)
    public ChatRoomDetailResponse getChatRoomDetail(Long chatRoomIdx) {
        User currentUser = securityUtils.getCurrentUser();

        chatParticipantRepository.findByChatRoom_IdxAndUser_Idx(chatRoomIdx, currentUser.getIdx())
                .orElseThrow(ChatRoomNotParticipantException::new);

        ChatRoom chatRoom = chatRoomRepository.findById(chatRoomIdx)
                .orElseThrow(ChatRoomNotFoundException::new);

        List<ChatParticipant> participants = chatParticipantRepository.findByChatRoom_Idx(chatRoomIdx);

        Map<Long, String> profileImageUrlMap = buildProfileImageUrlMap(participants);

        String imageUrl = chatRoom.getImageUuid() != null
                ? mediaService.getMediaUrl(chatRoom.getImageUuid()).getFileUrl()
                : null;

        return ChatRoomDetailResponse.from(chatRoom, participants, imageUrl, profileImageUrlMap);
    }

    @Transactional
    public void leaveChatRoom(Long chatRoomIdx) {
        User currentUser = securityUtils.getCurrentUser();

        chatParticipantRepository.findByChatRoom_IdxAndUser_Idx(chatRoomIdx, currentUser.getIdx())
                .orElseThrow(ChatRoomNotParticipantException::new);

        chatParticipantRepository.deleteByUser_IdxAndChatRoom_Idx(currentUser.getIdx(), chatRoomIdx);
    }

    @Transactional(readOnly = true)
    public ChatMessageListResponse getMessages(Long chatRoomIdx, Long cursorIdx, int size) {
        User currentUser = securityUtils.getCurrentUser();

        chatParticipantRepository.findByChatRoom_IdxAndUser_Idx(chatRoomIdx, currentUser.getIdx())
                .orElseThrow(ChatRoomNotParticipantException::new);

        PageRequest pageRequest = PageRequest.of(0, size + 1);

        List<ChatMessage> messages = (cursorIdx == null)
                ? chatMessageRepository.findByChatRoom_IdxOrderByIdxDesc(chatRoomIdx, pageRequest)
                : chatMessageRepository.findByChatRoom_IdxAndIdxLessThanOrderByIdxDesc(chatRoomIdx, cursorIdx, pageRequest);

        boolean hasNext = messages.size() > size;
        List<ChatMessage> pageMessages = hasNext ? messages.subList(0, size) : messages;

        Long nextCursor = hasNext ? pageMessages.get(pageMessages.size() - 1).getIdx() : null;

        Map<Long, String> senderProfileUrlMap = new HashMap<>();
        pageMessages.stream()
                .map(ChatMessage::getSender)
                .distinct()
                .forEach(sender -> mediaService.getProfileImageUrl(sender.getIdx())
                        .ifPresent(url -> senderProfileUrlMap.put(sender.getIdx(), url)));

        List<ChatMessageResponse> messageResponses = pageMessages.stream()
                .map(m -> {
                    String fileUrl = (m.getFile() != null)
                            ? mediaService.getMediaUrl(m.getFile().getUuid()).getFileUrl()
                            : null;
                    return ChatMessageResponse.from(m, senderProfileUrlMap.get(m.getSender().getIdx()), fileUrl);
                })
                .toList();

        return ChatMessageListResponse.builder()
                .messages(messageResponses)
                .nextCursor(nextCursor)
                .hasNext(hasNext)
                .build();
    }

    @Transactional
    public ReadMarkResponse markAsRead(Long chatRoomIdx, ReadMarkRequest request) {
        User currentUser = securityUtils.getCurrentUser();

        ChatParticipant participant = chatParticipantRepository
                .findByChatRoom_IdxAndUser_Idx(chatRoomIdx, currentUser.getIdx())
                .orElseThrow(ChatRoomNotParticipantException::new);

        if (!chatMessageRepository.existsByChatRoom_IdxAndIdx(chatRoomIdx, request.getLastReadMessageIdx())) {
            throw new ChatNotFoundException();
        }

        participant.setLastReadMessageIdx(request.getLastReadMessageIdx());

        return ReadMarkResponse.from(participant);
    }

    @Transactional
    public ChatMessageResponse sendMessage(Long chatRoomIdx, SendMessageRequest request) {
        User currentUser = securityUtils.getCurrentUser();

        chatParticipantRepository.findByChatRoom_IdxAndUser_Idx(chatRoomIdx, currentUser.getIdx())
                .orElseThrow(ChatRoomNotParticipantException::new);

        ChatRoom chatRoom = chatRoomRepository.findById(chatRoomIdx)
                .orElseThrow(ChatRoomNotFoundException::new);

        Media file = null;
        if (request.getFileUuid() != null) {
            file = mediaRepository.findByUuid(request.getFileUuid())
                    .orElseThrow(MediaNotFoundException::new);
        }

        ChatMessage chatMessage = chatMessageRepository.save(ChatMessage.builder()
                .sender(currentUser)
                .chatRoom(chatRoom)
                .message(request.getMessage())
                .type(request.getType())
                .file(file)
                .isDeleted(false)
                .build());

        String senderProfileUrl = mediaService.getProfileImageUrl(currentUser.getIdx()).orElse(null);
        String fileUrl = file != null ? mediaService.getMediaUrl(file.getUuid()).getFileUrl() : null;

        return ChatMessageResponse.from(chatMessage, senderProfileUrl, fileUrl);
    }

    private Map<Long, String> buildProfileImageUrlMap(List<ChatParticipant> participants) {
        Map<Long, String> map = new HashMap<>();
        participants.forEach(cp -> mediaService.getProfileImageUrl(cp.getUser().getIdx())
                .ifPresent(url -> map.put(cp.getUser().getIdx(), url)));
        return map;
    }
}
