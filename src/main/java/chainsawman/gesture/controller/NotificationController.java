package chainsawman.gesture.controller;

import chainsawman.gesture.dto.notification.request.NotificationCreateRequest;
import chainsawman.gesture.dto.notification.response.NotificationCreateResponse;
import chainsawman.gesture.dto.notification.response.NotificationListResponse;
import chainsawman.gesture.dto.notification.response.NotificationReadResponse;
import chainsawman.gesture.global.ApiResponse;
import chainsawman.gesture.service.NotificationService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/notifications")
@RequiredArgsConstructor
@Tag(name = "Notification", description = "알림 관련 API")
public class NotificationController {

    private final NotificationService notificationService;

    @PostMapping
    @Operation(summary = "알림 생성", description = "알림 생성 시 사용하는  API 입니다.")
    public ResponseEntity<ApiResponse<NotificationCreateResponse>> createNotification(
            @Valid @RequestBody NotificationCreateRequest request) {
        NotificationCreateResponse result = notificationService.createNotification(request);
        return ResponseEntity.ok(ApiResponse.ok(result, "생성되었습니다."));
    }

    @GetMapping
    @Operation(summary = "알림 목록 조회", description = "알림 목록 조회 시 사용하는 API 입니다.")
    public ResponseEntity<ApiResponse<List<NotificationListResponse>>> getNotifications() {
        List<NotificationListResponse> result = notificationService.getNotifications();
        return ResponseEntity.ok(ApiResponse.ok(result, "조회되었습니다."));
    }

    @PatchMapping("/{notificationIdx}/read")
    @Operation(summary = "알림 읽음 처리", description = "알림 읽음 처리 시 사용하는 API ")
    public ResponseEntity<ApiResponse<NotificationReadResponse>> readNotification
            (@PathVariable Long notificationIdx) {
        NotificationReadResponse result = notificationService.readNotification(notificationIdx);
        return ResponseEntity.ok(ApiResponse.ok(result, "요청이 성공적으로 처리되었습니다."));
    }

}
