package chainsawman.gesture.dto.media.response;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public class MediaUrlResponse {

    @JsonProperty("file_url")
    private String fileUrl;

}
