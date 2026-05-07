package chainsawman.gesture.service;

import chainsawman.gesture.dto.media.response.MediaUploadResponse;
import chainsawman.gesture.dto.media.response.MediaUrlResponse;
import chainsawman.gesture.entity.media.Media;
import chainsawman.gesture.entity.user.User;
import chainsawman.gesture.enums.MediaEntityType;
import chainsawman.gesture.exceptions.media.MediaNotFoundException;
import chainsawman.gesture.repository.media.MediaRepository;
import chainsawman.gesture.security.SecurityUtils;
import com.amazonaws.services.s3.AmazonS3;
import com.amazonaws.services.s3.model.ObjectMetadata;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.Collection;
import java.util.Map;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class MediaService {

    private final AmazonS3 amazonS3;
    private final MediaRepository mediaRepository;
    private final SecurityUtils securityUtils;

    @Value("${aws.s3.bucket}")
    private String bucket;


    // 파일 업로드
    @Transactional
    public MediaUploadResponse upload(MultipartFile file, MediaEntityType type) throws IOException {
        User user = securityUtils.getCurrentUser();
        String uuid = UUID.randomUUID().toString();
        String ext = getExtension(file.getOriginalFilename());
        String key = type.getPrefix() + "/" + user.getIdx() + "/" + uuid + ext;

        ObjectMetadata metadata = new ObjectMetadata();
        metadata.setContentType(file.getContentType());
        metadata.setContentLength(file.getSize());
        amazonS3.putObject(bucket, key, file.getInputStream(), metadata);

        String fileUrl = amazonS3.getUrl(bucket, key).toString();

        if (type == MediaEntityType.PROFILE) {
            mediaRepository.findByUser_IdxAndEntityType(user.getIdx(), MediaEntityType.PROFILE)
                    .ifPresent(existing -> {
                        amazonS3.deleteObject(bucket, existing.getFile());
                        mediaRepository.delete(existing);
                    });
        }

        Media media = new Media();
        media.setUser(user);
        media.setEntityType(type);
        media.setUuid(uuid);
        media.setName(file.getOriginalFilename());
        media.setFile(key);
        mediaRepository.save(media);

        return new MediaUploadResponse(uuid, fileUrl);
    }

    // 파일 URL 조회
    public MediaUrlResponse getMediaUrl(String uuid) {
        Media media = mediaRepository.findByUuid(uuid)
                .orElseThrow(MediaNotFoundException::new);
        String fileUrl = amazonS3.getUrl(bucket, media.getFile()).toString();
        return new MediaUrlResponse(fileUrl);
    }

    public Map<String, String> getMediaUrlMap(Collection<String> uuids) {
        return mediaRepository.findByUuidIn(uuids).stream()
                .collect(Collectors.toMap(
                        Media::getUuid,
                        media -> amazonS3.getUrl(bucket, media.getFile()).toString()
                ));
    }

    private String getExtension(String filename) {
        if (filename == null || !filename.contains(".")) return "";
        return filename.substring(filename.lastIndexOf("."));
    }
}
