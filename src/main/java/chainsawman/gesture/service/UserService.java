package chainsawman.gesture.service;

import chainsawman.gesture.dto.user.response.MyProfileResponse;
import chainsawman.gesture.dto.user.response.ProfileResponse;
import chainsawman.gesture.entity.user.User;
import chainsawman.gesture.exceptions.user.UserNotFoundException;
import chainsawman.gesture.repository.media.MediaRepository;
import chainsawman.gesture.repository.user.UserRepository;
import chainsawman.gesture.security.SecurityUtils;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class UserService {

    private final UserRepository userRepository;
    private final MediaRepository mediaRepository;
    private final SecurityUtils securityUtils;

    public ProfileResponse getProfile(Long userIdx) {
        User user = userRepository.findByIdxAndIsDeactivatedFalse(userIdx)
                .orElseThrow(UserNotFoundException::new);

        String profileUrl = mediaRepository.findByUser_Idx(user.getIdx())
                .map(media -> "/media/" + media.getUrl())
                .orElse(null);

        return ProfileResponse.from(user, profileUrl);
    }

    public MyProfileResponse getMyProfile() {
        User user = securityUtils.getCurrentUser();

        String profileUrl = mediaRepository.findByUser_Idx(user.getIdx())
                .map(media -> "/media/" + media.getUrl())
                .orElse(null);

        return MyProfileResponse.from(user, profileUrl);
    }
}
