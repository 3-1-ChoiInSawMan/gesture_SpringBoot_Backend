package chainsawman.gesture.service;

import chainsawman.gesture.dto.user.request.LoginRequest;
import chainsawman.gesture.dto.user.request.RegisterRequest;
import chainsawman.gesture.dto.user.response.LoginResponse;
import chainsawman.gesture.dto.user.response.RegisterResponse;
import chainsawman.gesture.entity.user.RefreshToken;
import chainsawman.gesture.entity.user.User;
import chainsawman.gesture.exceptions.user.DuplicateEmailException;
import chainsawman.gesture.exceptions.user.DuplicateIdException;
import chainsawman.gesture.exceptions.user.InvalidPasswordException;
import chainsawman.gesture.exceptions.user.UserNotFoundException;
import chainsawman.gesture.global.TokenProvider;
import chainsawman.gesture.repository.media.MediaRepository;
import chainsawman.gesture.repository.user.RefreshTokenRepository;
import chainsawman.gesture.repository.user.UserRepository;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;

@Service
@RequiredArgsConstructor
@Transactional
public class AuthService {
    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final TokenProvider tokenProvider;
    private final MediaRepository mediaRepository;
    private final RefreshTokenRepository refreshTokenRepository;


    // 로그인
    public LoginResponse login(LoginRequest request) {
        User user = userRepository.findByEmailAndDeactivatedFalse(request.getEmail())
                .orElseThrow(() -> new UserNotFoundException());

        if (!passwordEncoder.matches(request.getPassword(), user.getPassword())) {
            throw new InvalidPasswordException();
        }

        String accessToken = tokenProvider.createToken(user.getEmail());
        String refreshToken = tokenProvider.createRefreshToken(user.getEmail());

        saveRefreshToken(user, refreshToken);

        String profileUrl = mediaRepository.findByUser_Idx(user.getIdx())
                .map(media -> "/media/" + media.getUrl())
                .orElse(null);

        return LoginResponse.builder()
                .idx(user.getIdx())
                .id(user.getId())
                .email(user.getEmail())
                .nickname(user.getNickname())
                .profileUrl(profileUrl)
                .statusMessage(user.getStatusMessage())
                .provider(user.getProvider() != null ? user.getProvider().name() : null)
                .isDeactivated(user.getIsDeactivated())
                .accessToken(accessToken)
                .refreshToken(refreshToken)
                .createdAt(user.getCreatedAt())
                .updatedAt(user.getUpdatedAt())
                .build();
    }

    // 회원가입
    public RegisterResponse register(RegisterRequest request) {
        if (userRepository.existsByEmail(request.getEmail())) {
            throw new DuplicateEmailException();
        }

        if (userRepository.existsById(request.getId())) {
            throw new DuplicateIdException();
        }

        User user = new User();
        user.setId(request.getId());
        user.setEmail(request.getEmail());
        user.setPassword(passwordEncoder.encode(request.getPassword()));
        user.setNickname(request.getNickname());

        User saved = userRepository.save(user);

        return RegisterResponse.builder()
                .idx(saved.getIdx())
                .id(saved.getId())
                .email(saved.getEmail())
                .nickname(saved.getNickname())
                .profileUrl(null)
                .statusMessage(null)
                .provider(null)
                .isDeactivated(saved.getIsDeactivated())
                .createdAt(saved.getCreatedAt())
                .build();
    }

    // 리프레시 토큰 저장 (있으면 업데이트 없으면 생성)
    private void saveRefreshToken(User user, String refreshToken) {
        LocalDateTime expiresAt = LocalDateTime.now().plusDays(7);

        refreshTokenRepository.findByUser_Idx(user.getIdx()).ifPresentOrElse(
                existing -> existing.update(refreshToken, expiresAt),
                () -> refreshTokenRepository.save(new RefreshToken(user, refreshToken, expiresAt))
        );
    }
}
