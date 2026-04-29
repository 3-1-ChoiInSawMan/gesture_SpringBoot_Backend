package chainsawman.gesture.dto.media.response;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Getter;

@Getter
@JsonInclude(JsonInclude.Include.NON_NULL)
public class MediaUploadResponse {

    @JsonProperty("file_url")
    private final String fileUrl;

    @JsonProperty("profile_url")
    private final String profileUrl;

    public MediaUploadResponse(String fileUrl) {
        this.fileUrl = fileUrl;
        this.profileUrl = null;
    }

    public MediaUploadResponse(String fileUrl, String profileUrl) {
        this.fileUrl = fileUrl;
        this.profileUrl = profileUrl;
    }
}
