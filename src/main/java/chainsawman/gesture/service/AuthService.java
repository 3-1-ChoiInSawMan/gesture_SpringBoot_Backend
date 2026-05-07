package chainsawman.gesture.service;

import chainsawman.gesture.dto.user.request.*;
import chainsawman.gesture.dto.user.response.*;
import chainsawman.gesture.entity.user.RefreshToken;
import chainsawman.gesture.entity.user.User;
import chainsawman.gesture.enums.ProviderType;
import chainsawman.gesture.exceptions.auth.InvalidRefreshTokenException;
import chainsawman.gesture.exceptions.user.DeactivatedUserException;
import chainsawman.gesture.exceptions.user.DuplicateEmailException;
import chainsawman.gesture.exceptions.user.DuplicateIdException;
import chainsawman.gesture.exceptions.user.DuplicateSocialAccountException;
import chainsawman.gesture.exceptions.user.InvalidPasswordException;
import chainsawman.gesture.exceptions.user.SocialEmailConflictException;
import chainsawman.gesture.exceptions.user.UserNotFoundException;
import chainsawman.gesture.global.TokenProvider;
import chainsawman.gesture.repository.user.RefreshTokenRepository;
import chainsawman.gesture.repository.user.UserRepository;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.Optional;

@Service
@RequiredArgsConstructor
@Transactional
public class AuthService {
    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final TokenProvider tokenProvider;
    private final MediaService mediaService;
    private final RefreshTokenRepository refreshTokenRepository;


    // 로그인
    public LoginResponse login(LoginRequest request) {
        User user = userRepository.findByEmailAndIsDeactivatedFalse(request.getEmail())
                .orElseThrow(() -> new UserNotFoundException());

        if (!passwordEncoder.matches(request.getPassword(), user.getPassword())) {
            throw new InvalidPasswordException();
        }

        String accessToken = tokenProvider.createToken(user.getEmail(), user.getIdx());
        String refreshToken = tokenProvider.createRefreshToken(user.getEmail(), user.getIdx());

        saveRefreshToken(user, refreshToken);

        String profileUrl = mediaService.getProfileImageUrl(user.getIdx()).orElse(null);

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

        User saved = userRepository.save(User.builder()
                .id(request.getId())
                .email(request.getEmail())
                .password(passwordEncoder.encode(request.getPassword()))
                .nickname(request.getNickname())
                .build());

        String profileUrl = null;
        if (request.getProfileImageUuid() != null) {
            profileUrl = mediaService.updateProfileImage(request.getProfileImageUuid(), saved);
        }

        return RegisterResponse.builder()
                .idx(saved.getIdx())
                .id(saved.getId())
                .email(saved.getEmail())
                .nickname(saved.getNickname())
                .profileUrl(profileUrl)
                .statusMessage(null)
                .provider(null)
                .isDeactivated(saved.getIsDeactivated())
                .createdAt(saved.getCreatedAt())
                .build();
    }

    // 소셜 로그인 처리
    public SocialLoginResponse socialLogin(SocialLoginRequest request) {
        ProviderType provider = ProviderType.valueOf(request.getProvider());

        // 동일 소셜 계정으로 기존 가입된 유저
        Optional<User> byProvider = userRepository.findByProviderAndProviderId(provider, request.getProviderId());
        if (byProvider.isPresent()) {
            User user = byProvider.get();
            if (Boolean.TRUE.equals(user.getIsDeactivated())) {
                throw new DeactivatedUserException();
            }
            return loginSocialUser(user);
        }

        // 동일 이메일로 가입된 유저 확인 → 계정 연동
        Optional<User> byEmail = userRepository.findByEmail(request.getEmail());
        if (byEmail.isPresent()) {
            User user = byEmail.get();
            if (Boolean.TRUE.equals(user.getIsDeactivated())) {
                throw new DeactivatedUserException();
            }
            // 이미 다른 소셜 계정이 연결된 경우
            if (user.getProvider() != null) {
                throw new SocialEmailConflictException();
            }
            // LOCAL 계정에 소셜 연동 후 로그인
            user.setProvider(provider);
            user.setProviderId(request.getProviderId());
            return loginSocialUser(user);
        }

        // 신규 유저 → 추가정보 입력 필요
        return SocialLoginResponse.newUser();
    }

    private SocialLoginResponse loginSocialUser(User user) {
        String profileUrl = mediaService.getProfileImageUrl(user.getIdx()).orElse(null);
        String accessToken = tokenProvider.createToken(user.getEmail(), user.getIdx());
        String refreshToken = tokenProvider.createRefreshToken(user.getEmail(), user.getIdx());
        saveRefreshToken(user, refreshToken);
        return SocialLoginResponse.existingUser(user, profileUrl, accessToken, refreshToken);
    }

    // 소셜 회원가입 완료
    public SocialRegisterResponse socialRegister(SocialRegisterRequest request) {
        ProviderType provider = ProviderType.valueOf(request.getProvider());

        if (userRepository.existsById(request.getId())) {
            throw new DuplicateIdException();
        }

        // 동시 요청 방어 - 이미 같은 provider_id로 가입되어 있는지 체크
        if (userRepository.findByProviderAndProviderId(provider, request.getProviderId()).isPresent()) {
            throw new DuplicateSocialAccountException();
        }

        User user = userRepository.save(User.builder()
                .id(request.getId())
                .email(request.getEmail())
                .nickname(request.getNickname())
                .provider(provider)
                .providerId(request.getProviderId())
                .isDeactivated(false)
                .build());

        if (request.getProfileImageUuid() != null) {
            mediaService.updateProfileImage(request.getProfileImageUuid(), user);
        }

        String accessToken = tokenProvider.createToken(user.getEmail(), user.getIdx());
        String refreshToken = tokenProvider.createRefreshToken(user.getEmail(), user.getIdx());

        saveRefreshToken(user, refreshToken);

        return SocialRegisterResponse.from(user, accessToken, refreshToken);
    }

    // 리프레시 토큰 검증
    public RefreshTokenValidationResponse refreshTokenValidation(RefreshTokenRequest request) {
        String refreshToken = request.getRefreshToken();

        RefreshToken storedToken = refreshTokenRepository.findByToken(refreshToken)
                .orElseThrow(InvalidRefreshTokenException::new);

        User user = userRepository.findByIdxAndIsDeactivatedFalse(storedToken.getUser().getIdx())
                .orElseThrow(UserNotFoundException::new);

        return RefreshTokenValidationResponse.from(user);
    }

    // 리프레시 토큰 삭제
    public void deleteRefreshToken(RefreshTokenRequest request) {
        String refreshToken = request.getRefreshToken();

        RefreshToken storedToken = refreshTokenRepository.findByToken(refreshToken)
                .orElseThrow(InvalidRefreshTokenException::new);

        refreshTokenRepository.delete(storedToken);

    }



    // 리프레시 토큰 저장 (있으면 업데이트 없으면 생성)
    private void saveRefreshToken(User user, String refreshToken) {
        LocalDateTime expiresAt = LocalDateTime.now().plusDays(7);

        refreshTokenRepository.findByUser_Idx(user.getIdx()).ifPresentOrElse(
                existing -> existing.update(refreshToken, expiresAt),
                () -> refreshTokenRepository.save(RefreshToken.builder()
                        .user(user)
                        .token(refreshToken)
                        .expiresAt(expiresAt)
                        .build())
        );
    }
}
