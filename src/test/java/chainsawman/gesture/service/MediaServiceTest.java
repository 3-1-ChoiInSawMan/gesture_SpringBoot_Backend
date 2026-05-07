package chainsawman.gesture.service;

import chainsawman.gesture.dto.media.response.MediaUploadResponse;
import chainsawman.gesture.entity.media.Media;
import chainsawman.gesture.entity.user.User;
import chainsawman.gesture.enums.MediaEntityType;
import chainsawman.gesture.exceptions.media.MediaNotFoundException;
import chainsawman.gesture.repository.media.MediaRepository;
import chainsawman.gesture.security.SecurityUtils;
import com.amazonaws.services.s3.AmazonS3;
import com.amazonaws.services.s3.model.PutObjectResult;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.test.util.ReflectionTestUtils;

import java.io.IOException;
import java.net.URL;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class MediaServiceTest {

    @Mock AmazonS3 amazonS3;
    @Mock MediaRepository mediaRepository;
    @Mock SecurityUtils securityUtils;

    @InjectMocks MediaService mediaService;

    private User user;
    private MockMultipartFile imageFile;

    @BeforeEach
    void setUp() {
        ReflectionTestUtils.setField(mediaService, "bucket", "test-bucket");

        user = new User();
        ReflectionTestUtils.setField(user, "idx", 1L);

        imageFile = new MockMultipartFile("file", "photo.jpg", "image/jpeg", new byte[]{1, 2, 3});
    }

    // ─── upload ───────────────────────────────────

    @Test
    @DisplayName("파일 업로드 - uploads/ 경로로 저장, entityType null")
    void upload_saves_with_uploads_path() throws IOException {
        given(securityUtils.getCurrentUser()).willReturn(user);
        given(amazonS3.putObject(anyString(), anyString(), any(), any())).willReturn(new PutObjectResult());
        given(amazonS3.getUrl(anyString(), anyString())).willReturn(new URL("https://s3.amazonaws.com/test-bucket/uploads/1/uuid.jpg"));
        ArgumentCaptor<Media> captor = ArgumentCaptor.forClass(Media.class);
        given(mediaRepository.save(captor.capture())).willAnswer(inv -> inv.getArgument(0));

        MediaUploadResponse response = mediaService.upload(imageFile);

        assertThat(response.getFileUrl()).isNotNull();
        Media saved = captor.getValue();
        assertThat(saved.getEntityType()).isNull();
        assertThat(saved.getUser()).isEqualTo(user);
        assertThat(saved.getName()).isEqualTo("photo.jpg");
        assertThat(saved.getFile()).startsWith("uploads/1/");
    }

    @Test
    @DisplayName("파일 업로드 - S3 key에 userIdx 포함")
    void upload_key_contains_user_idx() throws IOException {
        given(securityUtils.getCurrentUser()).willReturn(user);
        given(amazonS3.putObject(anyString(), anyString(), any(), any())).willReturn(new PutObjectResult());
        given(amazonS3.getUrl(anyString(), anyString())).willReturn(new URL("https://s3.amazonaws.com/test-bucket/uploads/1/uuid.jpg"));
        given(mediaRepository.save(any())).willAnswer(inv -> inv.getArgument(0));

        mediaService.upload(imageFile);

        ArgumentCaptor<String> keyCaptor = ArgumentCaptor.forClass(String.class);
        verify(amazonS3).putObject(eq("test-bucket"), keyCaptor.capture(), any(), any());
        assertThat(keyCaptor.getValue()).startsWith("uploads/1/");
    }

    // ─── updateProfileImage ───────────────────────

    @Test
    @DisplayName("updateProfileImage - 기존 프로필 없으면 새 미디어에 PROFILE 타입 부여")
    void updateProfileImage_no_existing() throws Exception {
        Media newMedia = new Media();
        newMedia.setFile("uploads/1/new-uuid.jpg");
        newMedia.setUuid("new-uuid");
        newMedia.setUser(user);

        given(mediaRepository.findFirstByUser_IdxAndEntityType(1L, MediaEntityType.PROFILE)).willReturn(Optional.empty());
        given(mediaRepository.findByUuid("new-uuid")).willReturn(Optional.of(newMedia));
        given(amazonS3.getUrl("test-bucket", "uploads/1/new-uuid.jpg"))
                .willReturn(new URL("https://s3.amazonaws.com/test-bucket/uploads/1/new-uuid.jpg"));

        mediaService.updateProfileImage("new-uuid", user);

        assertThat(newMedia.getEntityType()).isEqualTo(MediaEntityType.PROFILE);
        verify(mediaRepository).save(newMedia);
        verify(amazonS3, never()).deleteObject(anyString(), anyString());
    }

    @Test
    @DisplayName("updateProfileImage - 기존 프로필 있으면 S3 삭제 후 새 미디어에 PROFILE 타입 부여")
    void updateProfileImage_replaces_existing() throws Exception {
        Media existing = new Media();
        existing.setFile("uploads/1/old-uuid.jpg");

        Media newMedia = new Media();
        newMedia.setFile("uploads/1/new-uuid.jpg");
        newMedia.setUuid("new-uuid");
        newMedia.setUser(user);

        given(mediaRepository.findFirstByUser_IdxAndEntityType(1L, MediaEntityType.PROFILE)).willReturn(Optional.of(existing));
        given(mediaRepository.findByUuid("new-uuid")).willReturn(Optional.of(newMedia));
        given(amazonS3.getUrl("test-bucket", "uploads/1/new-uuid.jpg"))
                .willReturn(new URL("https://s3.amazonaws.com/test-bucket/uploads/1/new-uuid.jpg"));

        mediaService.updateProfileImage("new-uuid", user);

        verify(amazonS3).deleteObject("test-bucket", "uploads/1/old-uuid.jpg");
        verify(mediaRepository).delete(existing);
        assertThat(newMedia.getEntityType()).isEqualTo(MediaEntityType.PROFILE);
        verify(mediaRepository).save(newMedia);
    }

    @Test
    @DisplayName("updateProfileImage - 존재하지 않는 UUID면 MediaNotFoundException")
    void updateProfileImage_uuid_not_found() {
        given(mediaRepository.findFirstByUser_IdxAndEntityType(1L, MediaEntityType.PROFILE)).willReturn(Optional.empty());
        given(mediaRepository.findByUuid("bad-uuid")).willReturn(Optional.empty());

        assertThatThrownBy(() -> mediaService.updateProfileImage("bad-uuid", user))
                .isInstanceOf(MediaNotFoundException.class);
    }

    @Test
    @DisplayName("updateProfileImage - 다른 유저의 UUID면 MediaNotFoundException")
    void updateProfileImage_not_owner() {
        User otherUser = new User();
        ReflectionTestUtils.setField(otherUser, "idx", 99L);

        Media otherMedia = new Media();
        otherMedia.setFile("uploads/99/uuid.jpg");
        otherMedia.setUuid("other-uuid");
        otherMedia.setUser(otherUser);

        given(mediaRepository.findFirstByUser_IdxAndEntityType(1L, MediaEntityType.PROFILE)).willReturn(Optional.empty());
        given(mediaRepository.findByUuid("other-uuid")).willReturn(Optional.of(otherMedia));

        assertThatThrownBy(() -> mediaService.updateProfileImage("other-uuid", user))
                .isInstanceOf(MediaNotFoundException.class);
    }
}
