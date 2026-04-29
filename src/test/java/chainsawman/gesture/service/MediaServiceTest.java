package chainsawman.gesture.service;

import chainsawman.gesture.dto.media.response.MediaUploadResponse;
import chainsawman.gesture.entity.media.Media;
import chainsawman.gesture.entity.user.User;
import chainsawman.gesture.enums.MediaEntityType;
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
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.*;
import static org.mockito.Mockito.eq;

@ExtendWith(MockitoExtension.class)
class MediaServiceTest {

    @Mock AmazonS3 amazonS3;
    @Mock MediaRepository mediaRepository;
    @Mock SecurityUtils securityUtils;

    @InjectMocks MediaService mediaService;

    private User user;
    private MockMultipartFile imageFile;

    @BeforeEach
    void setUp() throws Exception {
        ReflectionTestUtils.setField(mediaService, "bucket", "test-bucket");

        user = new User();
        ReflectionTestUtils.setField(user, "idx", 1L);

        imageFile = new MockMultipartFile("file", "photo.jpg", "image/jpeg", new byte[]{1, 2, 3});

        given(securityUtils.getCurrentUser()).willReturn(user);
        given(amazonS3.putObject(anyString(), anyString(), any(), any())).willReturn(new PutObjectResult());
        given(amazonS3.getUrl(anyString(), anyString())).willReturn(new URL("https://s3.amazonaws.com/test-bucket/profiles/1/uuid.jpg"));
    }

    @Test
    @DisplayName("PROFILE 업로드 - 기존 미디어 없을 때 새로 저장")
    void upload_profile_no_existing() throws IOException {
        given(mediaRepository.findByUser_IdxAndEntityType(1L, MediaEntityType.PROFILE)).willReturn(Optional.empty());

        MediaUploadResponse response = mediaService.upload(imageFile, MediaEntityType.PROFILE);

        assertThat(response.getFileUrl()).isNotNull();

        ArgumentCaptor<Media> captor = ArgumentCaptor.forClass(Media.class);
        verify(mediaRepository).save(captor.capture());
        Media saved = captor.getValue();
        assertThat(saved.getEntityType()).isEqualTo(MediaEntityType.PROFILE);
        assertThat(saved.getUser()).isEqualTo(user);
        assertThat(saved.getName()).isEqualTo("photo.jpg");
        assertThat(saved.getFile()).contains("profiles/1/");

        verify(amazonS3, never()).deleteObject(anyString(), anyString());
    }

    @Test
    @DisplayName("PROFILE 업로드 - 기존 미디어 있으면 S3 삭제 후 교체")
    void upload_profile_replaces_existing() throws IOException {
        Media existing = new Media();
        existing.setFile("profiles/1/old-uuid.jpg");
        given(mediaRepository.findByUser_IdxAndEntityType(1L, MediaEntityType.PROFILE)).willReturn(Optional.of(existing));

        MediaUploadResponse response = mediaService.upload(imageFile, MediaEntityType.PROFILE);

        verify(amazonS3).deleteObject("test-bucket", "profiles/1/old-uuid.jpg");
        verify(mediaRepository).delete(existing);

        ArgumentCaptor<Media> captor = ArgumentCaptor.forClass(Media.class);
        verify(mediaRepository).save(captor.capture());
        assertThat(captor.getValue().getEntityType()).isEqualTo(MediaEntityType.PROFILE);

    }

    @Test
    @DisplayName("ROOM 업로드 - DB 저장")
    void upload_room_saves_to_db() throws IOException {
        given(amazonS3.getUrl(anyString(), anyString())).willReturn(new URL("https://s3.amazonaws.com/test-bucket/rooms/1/uuid.jpg"));

        MediaUploadResponse response = mediaService.upload(imageFile, MediaEntityType.ROOM);

        assertThat(response.getFileUrl()).isNotNull();

        ArgumentCaptor<Media> captor = ArgumentCaptor.forClass(Media.class);
        verify(mediaRepository).save(captor.capture());
        assertThat(captor.getValue().getEntityType()).isEqualTo(MediaEntityType.ROOM);

        verify(mediaRepository, never()).findByUser_IdxAndEntityType(anyLong(), eq(MediaEntityType.ROOM));
    }

    @Test
    @DisplayName("CHAT 업로드 - DB 저장")
    void upload_chat_saves_to_db() throws IOException {
        given(amazonS3.getUrl(anyString(), anyString())).willReturn(new URL("https://s3.amazonaws.com/test-bucket/chats/1/uuid.jpg"));

        MediaUploadResponse response = mediaService.upload(imageFile, MediaEntityType.CHAT);

        ArgumentCaptor<Media> captor = ArgumentCaptor.forClass(Media.class);
        verify(mediaRepository).save(captor.capture());
        assertThat(captor.getValue().getEntityType()).isEqualTo(MediaEntityType.CHAT);
    }

    @Test
    @DisplayName("QUICK_SLOT 업로드 - DB 저장")
    void upload_quick_slot_saves_to_db() throws IOException {
        given(amazonS3.getUrl(anyString(), anyString())).willReturn(new URL("https://s3.amazonaws.com/test-bucket/quick-slots/1/uuid.jpg"));

        MediaUploadResponse response = mediaService.upload(imageFile, MediaEntityType.QUICK_SLOT);

        ArgumentCaptor<Media> captor = ArgumentCaptor.forClass(Media.class);
        verify(mediaRepository).save(captor.capture());
        Media saved = captor.getValue();
        assertThat(saved.getEntityType()).isEqualTo(MediaEntityType.QUICK_SLOT);
        assertThat(saved.getFile()).contains("quick-slots/1/");
    }

    @Test
    @DisplayName("S3 key에 userIdx가 포함된다")
    void upload_key_contains_user_idx() throws IOException {
        given(mediaRepository.findByUser_IdxAndEntityType(1L, MediaEntityType.PROFILE)).willReturn(Optional.empty());

        mediaService.upload(imageFile, MediaEntityType.PROFILE);

        ArgumentCaptor<String> keyCaptor = ArgumentCaptor.forClass(String.class);
        verify(amazonS3).putObject(eq("test-bucket"), keyCaptor.capture(), any(), any());
        assertThat(keyCaptor.getValue()).startsWith("profiles/1/");
    }
}
