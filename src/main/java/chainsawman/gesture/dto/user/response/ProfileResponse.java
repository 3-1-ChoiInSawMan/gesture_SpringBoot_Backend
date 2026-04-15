package chainsawman.gesture.dto.user.response;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
public class ProfileResponse {
    private Long idx;
    private String id;
    private String nickname;
    @JsonProperty("profile_url")
    private String profileUrl;
    @JsonProperty("status_message")
    private String statusMessage;
    private Boolean s
    {
        "success": true,
            "data": {
        "idx": 1,
                "id": "yoon123",
                "nickname": "윤정",
                "profile_url": "https://cdn.example.com/media/abc123.jpg",
                "status_message": "안녕하세요",
                "is_deactivated": false,
                "created_at": "2026-04-08T13:32:57"
    },
        "message": "요청이 성공적으로 처리되었습니다."
    }
}
