package chainsawman.gesture.service;

import chainsawman.gesture.dto.media.response.MediaUploadResponse;
import chainsawman.gesture.security.SecurityUtils;
import com.amazonaws.services.s3.AmazonS3;
import com.amazonaws.services.s3.model.ObjectMetadata;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class MediaService {

    private final AmazonS3 amazonS3;
    private final SecurityUtils securityUtils;

    @Value("${aws.s3.bucket}")
    private String bucket;

    public MediaUploadResponse upload(MultipartFile file, MediaEntityType type) throws IOException {
        Long userIdx = securityUtils.getCurrentUser().getIdx();
        String ext = getExtension(file.getOriginalFilename());
        String key = type.getPrefix() + "/" + userIdx + "/" + UUID.randomUUID() + ext;

        ObjectMetadata metadata = new ObjectMetadata();
        metadata.setContentType(file.getContentType());
        metadata.setContentLength(file.getSize());

        amazonS3.putObject(bucket, key, file.getInputStream(), metadata);

        String fileUrl = amazonS3.getUrl(bucket, key).toString();
        return new MediaUploadResponse(fileUrl);
    }

    private String getExtension(String filename) {
        if (filename == null || !filename.contains(".")) return "";
        return filename.substring(filename.lastIndexOf("."));
    }
}
