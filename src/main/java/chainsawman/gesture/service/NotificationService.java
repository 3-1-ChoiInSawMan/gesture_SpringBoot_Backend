package chainsawman.gesture.service;

import chainsawman.gesture.dto.notification.request.NotificationCreateRequest;
import chainsawman.gesture.dto.notification.response.NotificationCreateResponse;
import chainsawman.gesture.dto.notification.response.NotificationListResponse;
import chainsawman.gesture.dto.notification.response.NotificationReadResponse;
import chainsawman.gesture.entity.notification.Notification;
import chainsawman.gesture.entity.user.User;
import chainsawman.gesture.enums.NotificationType;
import chainsawman.gesture.exceptions.notification.NotificationNotFoundException;
import chainsawman.gesture.exceptions.user.UserNotFoundException;
import chainsawman.gesture.repository.notification.NotificationRepository;
import chainsawman.gesture.repository.user.UserRepository;
import chainsawman.gesture.security.SecurityUtils;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
public class NotificationService {

    private final NotificationRepository notificationRepository;
    private final UserRepository userRepository;
    private final SecurityUtils securityUtils;


    // 알림 생성
    @Transactional
    public NotificationCreateResponse createNotification(NotificationCreateRequest request) {
        User receiver = userRepository.findByIdxAndIsDeactivatedFalse(request.getReceiverId())
                .orElseThrow(UserNotFoundException::new);

        NotificationType type = NotificationType.valueOf(request.getType().toUpperCase());

        User actor = null;
        if (request.getActorId() != null) {
            actor = userRepository.findByIdxAndIsDeactivatedFalse(request.getActorId())
                    .orElseThrow(UserNotFoundException::new);
        }

        Notification notification = new Notification();
        notification.setUser(receiver);
        notification.setActor(actor);
        notification.setType(type);
        notification.setContent(generateContent(type, actor));
        notification.setRead(false);
        if (request.getTargetId() != null) {
            notification.setTargetId(String.valueOf(request.getTargetId()));
        }

        notificationRepository.save(notification);

        return NotificationCreateResponse.from(notification);
    }

    // 알림 목록 조회
    @Transactional(readOnly = true)
    public List<NotificationListResponse> getNotifications() {
        User user = securityUtils.getCurrentUser();
        return notificationRepository.findByUserOrderByCreatedAtDesc(user)
                .stream()
                .map(NotificationListResponse::from)
                .toList();
    }

    // 알림 읽음 처리
    @Transactional
    public NotificationReadResponse readNotification(Long notificationIdx) {
        User user = securityUtils.getCurrentUser();
        Notification notification = notificationRepository.findByIdxAndUser(notificationIdx, user)
                .orElseThrow(NotificationNotFoundException::new);
        notification.setRead(true);
        return NotificationReadResponse.from(notification);
    }

    private String generateContent(NotificationType type, User actor) {
        if (actor == null) {
            return "서비스 공지사항이 있습니다.";
        }
        String actorInfo = actor.getNickname() + "(@" + actor.getId() + ")";
        return switch (type) {
            case FRIEND -> actorInfo + "님이 친구 요청을 보냈습니다.";
            case CALL_ROOM -> actorInfo + "님이 통화방에 초대했습니다.";
            case CHAT_NOTICE -> actorInfo + "님이 메시지를 보냈습니다.";
            case MENTION -> actorInfo + "님이 회원님을 멘션했습니다.";
            case SERVICE_NOTICE -> "서비스 공지사항이 있습니다.";
        };
    }
}
