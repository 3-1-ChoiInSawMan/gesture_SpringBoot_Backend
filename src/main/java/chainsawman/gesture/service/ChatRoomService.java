package chainsawman.gesture.service;

import chainsawman.gesture.dto.chatRoom.request.ChatRoomInviteRespondRequest;
import chainsawman.gesture.dto.chatRoom.request.ChatRoomRequest;
import chainsawman.gesture.dto.chatRoom.response.ChatRoomInviteRespondResponse;
import chainsawman.gesture.dto.chatRoom.response.ChatRoomResponse;
import chainsawman.gesture.entity.chat.ChatParticipant;
import chainsawman.gesture.entity.chat.ChatRoom;
import chainsawman.gesture.entity.chat.ChatRoomInvitation;
import chainsawman.gesture.entity.notification.Notification;
import chainsawman.gesture.entity.user.User;
import chainsawman.gesture.enums.InvitationStatus;
import chainsawman.gesture.enums.NotificationType;
import chainsawman.gesture.exceptions.chat.ChatRoomInvitationAlreadyRespondedException;
import chainsawman.gesture.exceptions.chat.ChatRoomInvitationNotFoundException;
import chainsawman.gesture.repository.chat.ChatParticipantRepository;
import chainsawman.gesture.repository.chat.ChatRoomInvitationRepository;
import chainsawman.gesture.repository.chat.ChatRoomRepository;
import chainsawman.gesture.repository.notification.NotificationRepository;
import chainsawman.gesture.repository.user.UserRepository;
import chainsawman.gesture.security.SecurityUtils;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
public class ChatRoomService {

    private final ChatRoomRepository chatRoomRepository;
    private final ChatParticipantRepository chatParticipantRepository;
    private final ChatRoomInvitationRepository chatRoomInvitationRepository;
    private final NotificationRepository notificationRepository;
    private final UserRepository userRepository;
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
}
