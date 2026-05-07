package chainsawman.gesture.dto.media.response;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
public class MediaUrlResponse {

    @JsonProperty("file_url")
    private String fileUrl;

}
