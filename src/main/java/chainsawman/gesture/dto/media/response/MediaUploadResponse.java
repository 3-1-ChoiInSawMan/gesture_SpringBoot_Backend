package chainsawman.gesture.dto.media.response;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
public class MediaUploadResponse {

    @JsonProperty("media_uuid")
    private String mediaUuid;

    @JsonProperty("file_url")
    private String fileUrl;
}
