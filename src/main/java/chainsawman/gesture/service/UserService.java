package chainsawman.gesture.service;

import chainsawman.gesture.dto.user.request.PatchMyProfileRequest;
import chainsawman.gesture.dto.user.request.PatchPasswordRequest;
import chainsawman.gesture.dto.user.response.*;
import chainsawman.gesture.entity.media.Media;
import chainsawman.gesture.entity.room.Room;
import chainsawman.gesture.entity.room.RoomMember;
import chainsawman.gesture.entity.user.User;
import chainsawman.gesture.enums.MediaEntityType;
import chainsawman.gesture.enums.RoomRole;
import chainsawman.gesture.exceptions.media.MediaNotFoundException;
import chainsawman.gesture.exceptions.user.InvalidPasswordException;
import chainsawman.gesture.exceptions.user.UserNotFoundException;
import chainsawman.gesture.repository.media.MediaRepository;
import chainsawman.gesture.repository.room.RoomMemberRepository;
import chainsawman.gesture.repository.room.RoomRepository;
import chainsawman.gesture.repository.user.UserRepository;
import chainsawman.gesture.security.SecurityUtils;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class UserService {

    private final UserRepository userRepository;
    private final MediaRepository mediaRepository;
    private final RoomRepository roomRepository;
    private final RoomMemberRepository roomMemberRepository;
    private final SecurityUtils securityUtils;
    private final PasswordEncoder passwordEncoder;

    // 프로필 조회
    public ProfileResponse getProfile(Long userIdx) {
        User user = userRepository.findByIdxAndIsDeactivatedFalse(userIdx)
                .orElseThrow(UserNotFoundException::new);

        String profileUrl = mediaRepository.findByUser_IdxAndEntityType(user.getIdx(), MediaEntityType.PROFILE)
                .map(media -> "/media/" + media.getUuid())
                .orElse(null);

        return ProfileResponse.from(user, profileUrl);

    }

    // 내 프로필 조회
    public MyProfileResponse getMyProfile() {
        User user = securityUtils.getCurrentUser();

        String profileUrl = mediaRepository.findByUser_IdxAndEntityType(user.getIdx(), MediaEntityType.PROFILE)
                .map(media -> "/media/" + media.getUuid())
                .orElse(null);

        return MyProfileResponse.from(user, profileUrl);
    }

    // 유저 삭제(회원 탈퇴)
    @Transactional
    public WithdrawResponse deleteUser() {
        User user = securityUtils.getCurrentUser();

        List<Room> hostedRooms = roomRepository.findAllByHost_Idx(user.getIdx());
        for (Room room : hostedRooms) {
            roomMemberRepository.findTopByRoom_IdxAndUser_IdxNotOrderByCreatedAtAsc(room.getIdx(), user.getIdx())
                    .ifPresentOrElse(
                            nextMember -> {
                                nextMember.setRole(RoomRole.HOST);
                                room.setHost(nextMember.getUser());
                                roomMemberRepository.save(nextMember);
                                roomRepository.save(room);
                                roomMemberRepository.deleteByRoom_IdxAndUser_Idx(room.getIdx(), user.getIdx());
                            },
                            () -> {
                                roomMemberRepository.deleteAllByRoom_Idx(room.getIdx());
                                roomRepository.delete(room);
                            }
                    );
        }

        user.setIsDeactivated(true);
        userRepository.save(user);

        SecurityContextHolder.clearContext();
        return WithdrawResponse.from(user);
    }

    // 내 프로필 수정
    @Transactional
    public PatchMyProfileResponse patchMyProfile(PatchMyProfileRequest request) {
        User user = securityUtils.getCurrentUser();

        if (request.getNickname() != null) {
            user.setNickname(request.getNickname());
        }
        if (request.getStatusMessage() != null) {
            user.setStatusMessage(request.getStatusMessage());
        }
        userRepository.save(user);

        Optional<Media> mediaOptional = mediaRepository.findByUser_IdxAndEntityType(user.getIdx(), MediaEntityType.PROFILE);

        String profileUrl = mediaOptional
                .map(media -> "/media/" + media.getUuid())
                .orElse(null);

        if (request.getProfileUrl() != null) {
            Media media = mediaOptional.orElseThrow(MediaNotFoundException::new);
            media.setUuid(request.getProfileUrl());
            mediaRepository.save(media);
            profileUrl = "/media/" + request.getProfileUrl();
        }

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
    public List<UserResponse> getUser(String userId) {
        List<User> users = userRepository.findByIdContainingIgnoreCaseAndIsDeactivatedFalse(userId);

        if (users.isEmpty()) {
            return List.of();
        }

        List<String> userIds = users.stream()
                .map(User::getId)
                .toList();

        Map<String, String> profileUrlMap = mediaRepository.findByUser_IdInAndEntityType(userIds, MediaEntityType.PROFILE)
                .stream()
                .collect(Collectors.toMap(
                        media -> media.getUser().getId(),
                        media -> "/media/" + media.getUuid()
                ));
        // 매핑
        return users.stream()
                .map(user -> UserResponse.from(user, profileUrlMap.get(user.getId())))
                .toList();
    }


}
