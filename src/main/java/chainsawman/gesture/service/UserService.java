package chainsawman.gesture.service;

import chainsawman.gesture.dto.user.request.PatchMyProfileRequest;
import chainsawman.gesture.dto.user.request.PatchPasswordRequest;
import chainsawman.gesture.dto.user.response.*;
import chainsawman.gesture.entity.media.Media;
import chainsawman.gesture.entity.user.User;
import chainsawman.gesture.exceptions.media.MediaNotFoundException;
import chainsawman.gesture.exceptions.user.InvalidPasswordException;
import chainsawman.gesture.exceptions.user.UserNotFoundException;
import chainsawman.gesture.repository.media.MediaRepository;
import chainsawman.gesture.repository.user.UserRepository;
import chainsawman.gesture.security.SecurityUtils;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.util.Optional;

@Service
@RequiredArgsConstructor
public class UserService {

    private final UserRepository userRepository;
    private final MediaRepository mediaRepository;
    private final SecurityUtils securityUtils;
    private final PasswordEncoder passwordEncoder;

    // 프로필 조회
    public ProfileResponse getProfile(Long userIdx) {
        User user = userRepository.findByIdxAndIsDeactivatedFalse(userIdx)
                .orElseThrow(UserNotFoundException::new);

        String profileUrl = mediaRepository.findByUser_Idx(user.getIdx())
                .map(media -> "/media/" + media.getUrl())
                .orElse(null);

        return ProfileResponse.from(user, profileUrl);
    }

    // 내 프로필 조회
    public MyProfileResponse getMyProfile() {
        User user = securityUtils.getCurrentUser();

        String profileUrl = mediaRepository.findByUser_Idx(user.getIdx())
                .map(media -> "/media/" + media.getUrl())
                .orElse(null);

        return MyProfileResponse.from(user, profileUrl);
    }

    // 유저 삭제(회원 탈퇴)
    public WithdrawResponse deleteUser() {
        User user = securityUtils.getCurrentUser();
        user.setIsDeactivated(true);
        userRepository.save(user);

        SecurityContextHolder.clearContext();
        return WithdrawResponse.from(user);
    }

    // 내 프로필 수정
    public PatchMyProfileResponse patchMyProfile(PatchMyProfileRequest request) {
        User user = securityUtils.getCurrentUser();

        if (request.getNickname() != null) {
            user.setNickname(request.getNickname());
        }
        if (request.getStatusMessage() != null) {
            user.setStatusMessage(request.getStatusMessage());
        }
        userRepository.save(user);

        Optional<Media> mediaOptional = mediaRepository.findByUser_Idx(user.getIdx());

        if (request.getProfileUrl() != null) {
            Media media = mediaOptional.orElseThrow(MediaNotFoundException::new);
            media.setUrl(request.getProfileUrl());
            mediaRepository.save(media);
        }

        String profileUrl = mediaOptional
                .map(media -> "/media/" + media.getUrl())
                .orElse(null);

        return PatchMyProfileResponse.from(user, profileUrl);
    }

    // 내 비밀번호 변경
    public PatchPasswordResponse patchPassword(PatchPasswordRequest request) {
        User user = securityUtils.getCurrentUser();

        if (!passwordEncoder.matches(request.getCurrentPassword(), user.getPassword())) {
            throw new InvalidPasswordException();
        }

        user.setPassword(passwordEncoder.encode(request.getNewPassword()));
        userRepository.save(user);

        return PatchPasswordResponse.from(user);
    }

    // 사용자 검색
    public UserResponse getUser(Long userIdx) {
        User user = userRepository.findByIdxAndIsDeactivatedFalse(userIdx)
                .orElseThrow(UserNotFoundException::new);

        Optional<Media> mediaOptional = mediaRepository.findByUser_Idx(user.getIdx());

        String profileUrl = mediaOptional
                .map(media -> "/media/" + media.getUrl())
                .orElse(null);

        return UserResponse.from(user, profileUrl);
    }


}
