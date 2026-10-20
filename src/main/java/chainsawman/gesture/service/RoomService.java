package chainsawman.gesture.service;

import chainsawman.gesture.dto.room.request.RoomJoinRequest;
import chainsawman.gesture.dto.room.request.RoomPatchRequest;
import chainsawman.gesture.dto.room.request.RoomRequest;
import chainsawman.gesture.dto.room.response.*;
import chainsawman.gesture.entity.call.CallParticipant;
import chainsawman.gesture.entity.chat.ChatParticipant;
import chainsawman.gesture.entity.chat.ChatRoom;
import chainsawman.gesture.entity.room.Room;
import chainsawman.gesture.entity.room.RoomMember;
import chainsawman.gesture.entity.user.User;
import chainsawman.gesture.enums.RoomRole;
import chainsawman.gesture.enums.RoomType;
import chainsawman.gesture.exceptions.room.RoomAlreadyJoinedException;
import chainsawman.gesture.exceptions.room.RoomFullException;
import chainsawman.gesture.exceptions.room.RoomMaxParticipantExceededException;
import chainsawman.gesture.exceptions.room.RoomMemberNotFoundException;
import chainsawman.gesture.exceptions.room.NotRoomHostException;
import chainsawman.gesture.exceptions.room.RoomNotFoundException;
import chainsawman.gesture.exceptions.user.InvalidPasswordException;
import chainsawman.gesture.repository.call.CallParticipantRepository;
import chainsawman.gesture.repository.call.CallRepository;
import chainsawman.gesture.repository.chat.ChatParticipantRepository;
import chainsawman.gesture.repository.chat.ChatRoomRepository;
import chainsawman.gesture.repository.friend.FriendInviteRepository;
import chainsawman.gesture.repository.room.RoomMemberRepository;
import chainsawman.gesture.repository.room.RoomRepository;
import chainsawman.gesture.security.SecurityUtils;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class RoomService {

    private final RoomRepository roomRepository;
    private final RoomMemberRepository roomMemberRepository;
    private final ChatRoomRepository chatRoomRepository;
    private final ChatParticipantRepository chatParticipantRepository;
    private final CallRepository callRepository;
    private final CallParticipantRepository callParticipantRepository;
    private final FriendInviteRepository friendInviteRepository;
    private final MediaService mediaService;
    private final SecurityUtils securityUtils;
    private final PasswordEncoder passwordEncoder;

    @Transactional
    public RoomResponse createRoom(RoomRequest request) {
        User currentUser = securityUtils.getCurrentUser();

        String encodedPassword = null;
        if (!request.isPublicRoom() && request.getPassword() != null) {
            encodedPassword = passwordEncoder.encode(request.getPassword());
        }

        String thumbnailUrl = null;
        if (request.getThumbnailUuid() != null) {
            thumbnailUrl = mediaService.getMediaUrl(request.getThumbnailUuid()).getFileUrl();
        }

        ChatRoom chatRoom = chatRoomRepository.save(ChatRoom.builder()
                .name(request.getTitle())
                .build());

        chatParticipantRepository.save(ChatParticipant.builder()
                .chatRoom(chatRoom)
                .user(currentUser)
                .build());

        Room room = roomRepository.save(Room.builder()
                .host(currentUser)
                .title(request.getTitle())
                .category(parseCategory(request.getCategory()))
                .maxParticipant(request.getMaxParticipant())
                .isPublic(request.isPublicRoom())
                .password(encodedPassword)
                .thumbnailUrl(thumbnailUrl)
                .chatRoom(chatRoom)
                .build());

        roomMemberRepository.save(RoomMember.builder()
                .room(room)
                .user(currentUser)
                .role(RoomRole.HOST)
                .build());

        return RoomResponse.builder()
                .roomIdx(room.getIdx())
                .chatRoomIdx(chatRoom.getIdx())
                .title(room.getTitle())
                .category(categoryName(room.getCategory()))
                .maxParticipant(room.getMaxParticipant())
                .currentParticipant(1)
                .publicRoom(room.isPublic())
                .hasPassword(room.getPassword() != null)
                .thumbnailUrl(room.getThumbnailUrl())
                .hostUserIdx(currentUser.getIdx())
                .createdAt(room.getCreatedAt())
                .build();
    }

    @Transactional(readOnly = true)
    public Page<RoomListResponse> getRooms(Pageable pageable, RoomType category) {
        Page<Room> rooms = (category != null)
                ? roomRepository.findByCategoryAndDeletedAtIsNull(category, pageable)
                : roomRepository.findByDeletedAtIsNull(pageable);

        Map<String, String> profileUrlMap = fetchHostProfileUrls(rooms);

        return rooms.map(room -> RoomListResponse.from(
                room,
                roomMemberRepository.countByRoom_Idx(room.getIdx()),
                profileUrlMap.get(room.getHost().getId())
        ));
    }

    @Transactional(readOnly = true)
    public Page<RoomListResponse> searchRooms(Pageable pageable, String keyword, RoomType category) {
        Page<Room> rooms = (category != null)
                ? roomRepository.findByTitleContainingIgnoreCaseAndCategoryAndDeletedAtIsNull(keyword, category, pageable)
                : roomRepository.findByTitleContainingIgnoreCaseAndDeletedAtIsNull(keyword, pageable);

        Map<String, String> profileUrlMap = fetchHostProfileUrls(rooms);

        return rooms.map(room -> RoomListResponse.from(
                room,
                roomMemberRepository.countByRoom_Idx(room.getIdx()),
                profileUrlMap.get(room.getHost().getId())
        ));
    }

    @Transactional(readOnly = true)
    public RoomDetailResponse getRoomDetail(Long roomIdx) {
        Room room = roomRepository.findByIdxAndDeletedAtIsNull(roomIdx)
                .orElseThrow(RoomNotFoundException::new);
        int currentParticipant = roomMemberRepository.countByRoom_Idx(roomIdx);

        return RoomDetailResponse.builder()
                .roomIdx(room.getIdx())
                .title(room.getTitle())
                .category(categoryName(room.getCategory()))
                .maxParticipant(room.getMaxParticipant())
                .currentParticipant(currentParticipant)
                .publicRoom(room.isPublic())
                .hasPassword(room.getPassword() != null)
                .thumbnailUrl(room.getThumbnailUrl())
                .hostUserIdx(room.getHost().getIdx())
                .build();
    }

    @Transactional
    public RoomPatchResponse patchRoom(Long roomIdx, RoomPatchRequest request) {
        User currentUser = securityUtils.getCurrentUser();
        Room room = roomRepository.findByIdxAndDeletedAtIsNull(roomIdx)
                .orElseThrow(RoomNotFoundException::new);

        if (!room.getHost().getIdx().equals(currentUser.getIdx())) {
            throw new NotRoomHostException();
        }

        int currentCount = roomMemberRepository.countByRoom_Idx(roomIdx);

        if (request.getTitle() != null) room.setTitle(request.getTitle());
        if (request.getCategory() != null) room.setCategory(parseCategory(request.getCategory()));
        if (request.getMaxParticipant() > 0) {
            if (request.getMaxParticipant() < currentCount) {
                throw new RoomMaxParticipantExceededException(currentCount);
            }
            room.setMaxParticipant(request.getMaxParticipant());
        }
        if (request.getPublicRoom() != null) {
            room.setPublic(request.getPublicRoom());
        }
        if (request.getPassword() != null) {
            room.setPassword(passwordEncoder.encode(request.getPassword()));
        } else if (Boolean.TRUE.equals(request.getPublicRoom())) {
            room.setPassword(null);
        }
        if (request.getThumbnailUuid() != null) {
            room.setThumbnailUrl(mediaService.getMediaUrl(request.getThumbnailUuid()).getFileUrl());
        }
        roomRepository.save(room);

        return RoomPatchResponse.builder()
                .roomIdx(room.getIdx())
                .title(room.getTitle())
                .category(categoryName(room.getCategory()))
                .maxParticipant(room.getMaxParticipant())
                .currentParticipant(currentCount)
                .publicRoom(room.isPublic())
                .hasPassword(room.getPassword() != null)
                .thumbnailUrl(room.getThumbnailUrl())
                .hostUserIdx(room.getHost().getIdx())
                .createdAt(room.getCreatedAt())
                .updatedAt(room.getUpdatedAt())
                .build();
    }

    @Transactional
    public RoomDeleteResponse deleteRoom(Long roomIdx) {
        User currentUser = securityUtils.getCurrentUser();
        Room room = roomRepository.findByIdxAndDeletedAtIsNull(roomIdx)
                .orElseThrow(RoomNotFoundException::new);

        if (!room.getHost().getIdx().equals(currentUser.getIdx())) {
            throw new NotRoomHostException();
        }

        softDeleteRoom(room);

        return RoomDeleteResponse.builder()
                .deleted(true)
                .roomIdx(roomIdx)
                .deletedAt(room.getDeletedAt())
                .build();
    }

    @Transactional
    public RoomJoinResponse joinRoom(Long roomIdx, RoomJoinRequest request) {
        User currentUser = securityUtils.getCurrentUser();
        Room room = roomRepository.findByIdxAndDeletedAtIsNull(roomIdx)
                .orElseThrow(RoomNotFoundException::new);

        if (roomMemberRepository.existsByRoom_IdxAndUser_Idx(roomIdx, currentUser.getIdx())) {
            throw new RoomAlreadyJoinedException();
        }

        int currentParticipant = roomMemberRepository.countByRoom_Idx(roomIdx);
        if (currentParticipant >= room.getMaxParticipant()) {
            throw new RoomFullException();
        }

        if (!room.isPublic()) {
            String password = (request != null) ? request.getPassword() : null;
            if (password == null || !passwordEncoder.matches(password, room.getPassword())) {
                throw new InvalidPasswordException();
            }
        }

        RoomMember member = roomMemberRepository.save(RoomMember.builder()
                .room(room)
                .user(currentUser)
                .role(RoomRole.MEMBER)
                .build());

        if (room.getChatRoom() != null) {
            chatParticipantRepository.save(ChatParticipant.builder()
                    .chatRoom(room.getChatRoom())
                    .user(currentUser)
                    .build());
        }

        return RoomJoinResponse.builder()
                .roomMemberIdx(member.getIdx())
                .roomIdx(room.getIdx())
                .userIdx(currentUser.getIdx())
                .role(member.getRole().name())
                .joinedAt(member.getCreatedAt())
                .currentParticipant(currentParticipant + 1)
                .maxParticipant(room.getMaxParticipant())
                .build();
    }

    @Transactional
    public RoomLeaveResponse leaveRoom(Long roomIdx) {
        User currentUser = securityUtils.getCurrentUser();
        Room room = roomRepository.findByIdxAndDeletedAtIsNull(roomIdx)
                .orElseThrow(RoomNotFoundException::new);

        RoomMember member = roomMemberRepository.findByRoom_IdxAndUser_Idx(roomIdx, currentUser.getIdx())
                .orElseThrow(RoomMemberNotFoundException::new);

        if (room.getChatRoom() != null) {
            chatParticipantRepository.deleteByUser_IdxAndChatRoom_Idx(
                    currentUser.getIdx(), room.getChatRoom().getIdx());
        }

        if (member.getRole() == RoomRole.HOST) {
            Optional<RoomMember> nextMemberOpt = roomMemberRepository
                    .findTopByRoom_IdxAndUser_IdxNotOrderByCreatedAtAsc(roomIdx, currentUser.getIdx());

            if (nextMemberOpt.isPresent()) {
                RoomMember nextMember = nextMemberOpt.get();
                nextMember.setRole(RoomRole.HOST);
                room.setHost(nextMember.getUser());
                roomMemberRepository.save(nextMember);
                roomRepository.save(room);
                roomMemberRepository.deleteByRoom_IdxAndUser_Idx(roomIdx, currentUser.getIdx());

                return RoomLeaveResponse.builder()
                        .roomIdx(roomIdx)
                        .deleted(false)
                        .newHostIdx(nextMember.getUser().getIdx())
                        .build();
            } else {
                softDeleteRoom(room);

                return RoomLeaveResponse.builder()
                        .roomIdx(roomIdx)
                        .deleted(true)
                        .build();
            }
        }

        roomMemberRepository.deleteByRoom_IdxAndUser_Idx(roomIdx, currentUser.getIdx());

        return RoomLeaveResponse.builder()
                .roomIdx(roomIdx)
                .deleted(false)
                .build();
    }

    // 소프트 삭제: 통화 기록·회의록은 남기고 deletedAt만 기록. 멤버·친구 초대는 삭제
    @Transactional
    public void softDeleteRoom(Room room) {
        Long roomIdx = room.getIdx();
        LocalDateTime now = LocalDateTime.now();

        // 진행 중인 통화가 있으면 종료 처리 (참여자가 다른 통화에 못 들어가는 문제 방지)
        callRepository.findByRoom_IdxAndEndedAtIsNull(roomIdx).ifPresent(call -> {
            List<CallParticipant> participants = callParticipantRepository
                    .findByCall_IdxAndLeftAtIsNullOrderByJoinedAtAsc(call.getIdx());
            participants.forEach(p -> p.setLeftAt(now));
            callParticipantRepository.saveAll(participants);
            call.setEndedAt(now);
            callRepository.save(call);
        });

        friendInviteRepository.deleteAllByRoom_Idx(roomIdx);
        roomMemberRepository.deleteAllByRoom_Idx(roomIdx);
        room.setDeletedAt(now);
        roomRepository.save(room);
    }

    private Map<String, String> fetchHostProfileUrls(Page<Room> rooms) {
        List<String> hostIds = rooms.stream()
                .map(room -> room.getHost().getId())
                .distinct()
                .collect(Collectors.toList());
        return mediaService.getProfileImageUrlMap(hostIds);
    }

    private RoomType parseCategory(String category) {
        if (category == null) return null;
        try {
            return RoomType.valueOf(category.toUpperCase());
        } catch (IllegalArgumentException e) {
            return null;
        }
    }

    private String categoryName(RoomType category) {
        return category != null ? category.name().toLowerCase() : null;
    }
}
