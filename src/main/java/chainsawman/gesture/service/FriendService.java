package chainsawman.gesture.service;

import chainsawman.gesture.dto.friend.request.FriendRequestRespondRequest;
import chainsawman.gesture.dto.friend.response.FriendCountResponse;
import chainsawman.gesture.dto.friend.response.FriendDeleteResponse;
import chainsawman.gesture.dto.friend.response.FriendListResponse;
import chainsawman.gesture.dto.friend.response.FriendRequestListResponse;
import chainsawman.gesture.dto.friend.response.FriendRequestRespondResponse;
import chainsawman.gesture.dto.friend.response.FriendRequestSendResponse;
import chainsawman.gesture.entity.friend.Friendship;
import chainsawman.gesture.entity.user.User;
import chainsawman.gesture.enums.FriendshipStatus;
import chainsawman.gesture.exceptions.friend.FriendRequestNotFoundException;
import chainsawman.gesture.exceptions.friend.FriendshipNotFoundException;
import chainsawman.gesture.exceptions.friend.InvalidFriendRequestStatusException;
import chainsawman.gesture.exceptions.user.UserNotFoundException;
import chainsawman.gesture.repository.friend.FriendshipRepository;
import chainsawman.gesture.repository.user.UserRepository;
import chainsawman.gesture.security.SecurityUtils;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class FriendService {

    private final FriendshipRepository friendshipRepository;
    private final UserRepository userRepository;
    private final SecurityUtils securityUtils;

    @Transactional
    public FriendRequestSendResponse postFriend(Long receiverIdx) {
        User requester = securityUtils.getCurrentUser();
        User receiver = userRepository.findByIdxAndIsDeactivatedFalse(receiverIdx)
                .orElseThrow(UserNotFoundException::new);

        Friendship friendship = new Friendship();
        friendship.setUser(requester);
        friendship.setFriend(receiver);
        friendship.setStatus(FriendshipStatus.PENDING);
        friendshipRepository.save(friendship);

        return FriendRequestSendResponse.from(friendship);
    }

    @Transactional(readOnly = true)
    public List<FriendRequestListResponse> getFriendRequests() {
        User currentUser = securityUtils.getCurrentUser();

        return friendshipRepository.findAllByFriend_IdxAndStatus(currentUser.getIdx(), FriendshipStatus.PENDING)
                .stream()
                .map(FriendRequestListResponse::from)
                .collect(Collectors.toList());
    }

    @Transactional
    public FriendRequestRespondResponse respondToFriendRequest(Long friendshipIdx, FriendRequestRespondRequest request) {
        User currentUser = securityUtils.getCurrentUser();

        Friendship friendship = friendshipRepository.findByIdxAndFriend_Idx(friendshipIdx, currentUser.getIdx())
                .orElseThrow(FriendRequestNotFoundException::new);

        FriendshipStatus newStatus;
        try {
            newStatus = FriendshipStatus.valueOf(request.getStatus().toUpperCase());
        } catch (IllegalArgumentException e) {
            throw new InvalidFriendRequestStatusException();
        }
        if (newStatus == FriendshipStatus.PENDING) {
            throw new InvalidFriendRequestStatusException();
        }

        friendship.setStatus(newStatus);
        friendshipRepository.save(friendship);

        return FriendRequestRespondResponse.from(friendship);
    }

    @Transactional(readOnly = true)
    public List<FriendListResponse> getFriendList() {
        User currentUser = securityUtils.getCurrentUser();
        Long myIdx = currentUser.getIdx();

        List<FriendListResponse> result = new ArrayList<>();

        friendshipRepository.findAllByUser_IdxAndStatus(myIdx, FriendshipStatus.ACCEPTED)
                .forEach(f -> result.add(FriendListResponse.from(f.getFriend())));

        friendshipRepository.findAllByFriend_IdxAndStatus(myIdx, FriendshipStatus.ACCEPTED)
                .forEach(f -> result.add(FriendListResponse.from(f.getUser())));

        return result;
    }

    @Transactional(readOnly = true)
    public FriendCountResponse getFriendCount() {
        User currentUser = securityUtils.getCurrentUser();
        Long myIdx = currentUser.getIdx();

        long count = friendshipRepository.countByUser_IdxAndStatus(myIdx, FriendshipStatus.ACCEPTED)
                + friendshipRepository.countByFriend_IdxAndStatus(myIdx, FriendshipStatus.ACCEPTED);

        return FriendCountResponse.builder().count(count).build();
    }

    @Transactional
    public FriendDeleteResponse deleteFriend(Long targetUserIdx) {
        User currentUser = securityUtils.getCurrentUser();

        Friendship friendship = friendshipRepository
                .findByUser_IdxAndFriend_IdxAndStatus(currentUser.getIdx(), targetUserIdx, FriendshipStatus.ACCEPTED)
                .or(() -> friendshipRepository.findByUser_IdxAndFriend_IdxAndStatus(targetUserIdx, currentUser.getIdx(), FriendshipStatus.ACCEPTED))
                .orElseThrow(FriendshipNotFoundException::new);

        friendshipRepository.delete(friendship);

        return FriendDeleteResponse.builder()
                .deleted(true)
                .userIdx(currentUser.getIdx())
                .targetUserIdx(targetUserIdx)
                .deletedAt(LocalDateTime.now())
                .build();
    }
}
