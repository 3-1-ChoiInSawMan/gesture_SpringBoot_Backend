package chainsawman.gesture.service;

import chainsawman.gesture.dto.user.request.PatchMyProfileRequest;
import chainsawman.gesture.dto.user.response.MyProfileResponse;
import chainsawman.gesture.dto.user.response.PatchMyProfileResponse;
import chainsawman.gesture.entity.user.User;
import chainsawman.gesture.exceptions.media.MediaNotFoundException;
import chainsawman.gesture.exceptions.user.UserNotFoundException;
import chainsawman.gesture.repository.room.RoomMemberRepository;
import chainsawman.gesture.repository.room.RoomRepository;
import chainsawman.gesture.repository.user.UserRepository;
import chainsawman.gesture.security.SecurityUtils;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.test.util.ReflectionTestUtils;

import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;

@ExtendWith(MockitoExtension.class)
class UserServiceTest {

    @Mock UserRepository userRepository;
    @Mock MediaService mediaService;
    @Mock RoomRepository roomRepository;
    @Mock RoomMemberRepository roomMemberRepository;
    @Mock SecurityUtils securityUtils;
    @Mock PasswordEncoder passwordEncoder;

    @InjectMocks UserService userService;

    private User user;

    @BeforeEach
    void setUp() {
        user = new User();
        ReflectionTestUtils.setField(user, "idx", 1L);
        user.setNickname("기존닉네임");
    }

    // ─── getProfile ───────────────────────────────

    @Test
    @DisplayName("프로필 조회 - 프로필 이미지 없으면 profileUrl null")
    void getProfile_no_image() {
        given(userRepository.findByIdxAndIsDeactivatedFalse(1L)).willReturn(Optional.of(user));
        given(mediaService.getProfileImageUrl(1L)).willReturn(Optional.empty());

        var response = userService.getProfile(1L);

        assertThat(response.getProfileUrl()).isNull();
    }

    @Test
    @DisplayName("프로필 조회 - 존재하지 않는 유저면 UserNotFoundException")
    void getProfile_user_not_found() {
        given(userRepository.findByIdxAndIsDeactivatedFalse(99L)).willReturn(Optional.empty());

        assertThatThrownBy(() -> userService.getProfile(99L))
                .isInstanceOf(UserNotFoundException.class);
    }

    // ─── getMyProfile ─────────────────────────────

    @Test
    @DisplayName("내 프로필 조회 - S3 URL 반환")
    void getMyProfile_with_image() {
        given(securityUtils.getCurrentUser()).willReturn(user);
        given(mediaService.getProfileImageUrl(1L))
                .willReturn(Optional.of("https://s3.example.com/profile.jpg"));

        MyProfileResponse response = userService.getMyProfile();

        assertThat(response.getProfileUrl()).isEqualTo("https://s3.example.com/profile.jpg");
    }

    // ─── patchMyProfile ───────────────────────────

    @Test
    @DisplayName("프로필 수정 - UUID 없으면 프로필 이미지 변경 없음")
    void patchMyProfile_no_uuid() {
        given(securityUtils.getCurrentUser()).willReturn(user);
        given(userRepository.save(any())).willReturn(user);
        given(mediaService.getProfileImageUrl(1L)).willReturn(Optional.empty());

        PatchMyProfileRequest request = new PatchMyProfileRequest("새닉네임", null, null);
        PatchMyProfileResponse response = userService.patchMyProfile(request);

        assertThat(response.getNickname()).isEqualTo("새닉네임");
        verify(mediaService, never()).updateProfileImage(any(), any());
    }

    @Test
    @DisplayName("프로필 수정 - 유효한 UUID면 updateProfileImage 호출")
    void patchMyProfile_with_valid_uuid() {
        given(securityUtils.getCurrentUser()).willReturn(user);
        given(userRepository.save(any())).willReturn(user);
        given(mediaService.updateProfileImage("valid-uuid", user))
                .willReturn("https://s3.example.com/new.jpg");
        given(mediaService.getProfileImageUrl(1L))
                .willReturn(Optional.of("https://s3.example.com/new.jpg"));

        PatchMyProfileRequest request = new PatchMyProfileRequest(null, "valid-uuid", null);
        userService.patchMyProfile(request);

        verify(mediaService).updateProfileImage("valid-uuid", user);
    }

    @Test
    @DisplayName("프로필 수정 - 잘못된 UUID면 MediaNotFoundException")
    void patchMyProfile_invalid_uuid() {
        given(securityUtils.getCurrentUser()).willReturn(user);
        given(userRepository.save(any())).willReturn(user);
        given(mediaService.updateProfileImage("bad-uuid", user))
                .willThrow(new MediaNotFoundException());

        assertThatThrownBy(() -> userService.patchMyProfile(new PatchMyProfileRequest(null, "bad-uuid", null)))
                .isInstanceOf(MediaNotFoundException.class);
    }

    @Test
    @DisplayName("프로필 수정 - 다른 유저 UUID면 MediaNotFoundException")
    void patchMyProfile_other_user_uuid() {
        given(securityUtils.getCurrentUser()).willReturn(user);
        given(userRepository.save(any())).willReturn(user);
        given(mediaService.updateProfileImage("other-uuid", user))
                .willThrow(new MediaNotFoundException());

        assertThatThrownBy(() -> userService.patchMyProfile(new PatchMyProfileRequest(null, "other-uuid", null)))
                .isInstanceOf(MediaNotFoundException.class);
    }
}
