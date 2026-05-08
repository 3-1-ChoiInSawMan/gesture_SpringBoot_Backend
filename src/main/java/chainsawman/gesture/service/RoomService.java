package chainsawman.gesture.service;

import chainsawman.gesture.dto.room.request.RoomJoinRequest;
import chainsawman.gesture.dto.room.request.RoomPatchRequest;
import chainsawman.gesture.dto.room.request.RoomRequest;
import chainsawman.gesture.dto.room.response.*;
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
import chainsawman.gesture.exceptions.room.RoomNotFoundException;
import chainsawman.gesture.exceptions.user.InvalidPasswordException;
import chainsawman.gesture.repository.chat.ChatParticipantRepository;
import chainsawman.gesture.repository.chat.ChatRoomRepository;
import chainsawman.gesture.repository.room.RoomMemberRepository;
import chainsawman.gesture.repository.room.RoomRepository;
import chainsawman.gesture.security.SecurityUtils;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;

@Service
@RequiredArgsConstructor
public class RoomService {

    private final RoomRepository roomRepository;
    private final RoomMemberRepository roomMemberRepository;
    private final ChatRoomRepository chatRoomRepository;
    private final ChatParticipantRepository chatParticipantRepository;
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
                ? roomRepository.findByCategory(category, pageable)
                : roomRepository.findAll(pageable);

        return rooms.map(room -> toRoomListResponse(room, roomMemberRepository.countByRoom_Idx(room.getIdx())));
    }

    @Transactional(readOnly = true)
    public Page<RoomListResponse> searchRooms(Pageable pageable, String keyword, RoomType category) {
        Page<Room> rooms = (category != null)
                ? roomRepository.findByTitleContainingIgnoreCaseAndCategory(keyword, category, pageable)
                : roomRepository.findByTitleContainingIgnoreCase(keyword, pageable);
        return rooms.map(room -> toRoomListResponse(room, roomMemberRepository.countByRoom_Idx(room.getIdx())));
    }

    @Transactional(readOnly = true)
    public RoomDetailResponse getRoomDetail(Long roomIdx) {
        Room room = roomRepository.findById(roomIdx)
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
        Room room = roomRepository.findById(roomIdx)
                .orElseThrow(RoomNotFoundException::new);

        if (!room.getHost().getIdx().equals(currentUser.getIdx())) {
            throw new AccessDeniedException("방장만 수정할 수 있습니다.");
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
        Room room = roomRepository.findById(roomIdx)
                .orElseThrow(RoomNotFoundException::new);

        if (!room.getHost().getIdx().equals(currentUser.getIdx())) {
            throw new AccessDeniedException("방장만 삭제할 수 있습니다.");
        }

        roomMemberRepository.deleteAllByRoom_Idx(roomIdx);
        roomRepository.delete(room);

        return RoomDeleteResponse.builder()
                .deleted(true)
                .roomIdx(roomIdx)
                .deletedAt(LocalDateTime.now())
                .build();
    }

    @Transactional
    public RoomJoinResponse joinRoom(Long roomIdx, RoomJoinRequest request) {
        User currentUser = securityUtils.getCurrentUser();
        Room room = roomRepository.findById(roomIdx)
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

    private RoomListResponse toRoomListResponse(Room room, int currentParticipant) {
        return RoomListResponse.builder()
                .roomIdx(room.getIdx())
                .title(room.getTitle())
                .category(categoryName(room.getCategory()))
                .currentParticipant(currentParticipant)
                .maxParticipant(room.getMaxParticipant())
                .publicRoom(room.isPublic())
                .hasPassword(room.getPassword() != null)
                .thumbnailUrl(room.getThumbnailUrl())
                .hostUserIdx(room.getHost().getIdx())
                .build();
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
