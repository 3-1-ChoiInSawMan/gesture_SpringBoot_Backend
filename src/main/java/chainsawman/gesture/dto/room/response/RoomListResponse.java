package chainsawman.gesture.dto.room.response;

import chainsawman.gesture.entity.room.Room;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Builder;
import lombok.Getter;

import java.time.LocalDateTime;

@Getter
@Builder
public class RoomListResponse {
    @JsonProperty("room_idx")
    private Long roomIdx;

    private String title;
    private String category;

    @JsonProperty("current_participant")
    private int currentParticipant;

    @JsonProperty("max_participant")
    private int maxParticipant;

    @JsonProperty("is_public")
    private boolean publicRoom;

    @JsonProperty("has_password")
    private boolean hasPassword;

    @JsonProperty("thumbnail_url")
    private String thumbnailUrl;

    @JsonProperty("created_at")
    private LocalDateTime createdAt;

    private HostInfo host;

    public static RoomListResponse from(Room room, int currentParticipant, String hostProfileUrl) {
        return RoomListResponse.builder()
                .roomIdx(room.getIdx())
                .title(room.getTitle())
                .category(room.getCategory() != null ? room.getCategory().name().toLowerCase() : null)
                .currentParticipant(currentParticipant)
                .maxParticipant(room.getMaxParticipant())
                .publicRoom(room.isPublic())
                .hasPassword(room.getPassword() != null)
                .thumbnailUrl(room.getThumbnailUrl())
                .createdAt(room.getCreatedAt())
                .host(HostInfo.from(room.getHost(), hostProfileUrl))
                .build();
    }
}
