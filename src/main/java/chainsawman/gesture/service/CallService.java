package chainsawman.gesture.service;

import chainsawman.gesture.dto.call.response.CallJoinResponse;
import chainsawman.gesture.dto.call.response.CallLeaveResponse;
import chainsawman.gesture.dto.call.response.CallParticipantInfo;
import chainsawman.gesture.dto.call.response.CallParticipantsResponse;
import chainsawman.gesture.entity.call.Call;
import chainsawman.gesture.entity.call.CallParticipant;
import chainsawman.gesture.entity.room.Room;
import chainsawman.gesture.entity.user.User;
import chainsawman.gesture.exceptions.call.CallAlreadyJoinedException;
import chainsawman.gesture.exceptions.call.CallParticipantNotFoundException;
import chainsawman.gesture.exceptions.call.NoActiveCallException;
import chainsawman.gesture.exceptions.room.RoomMemberNotFoundException;
import chainsawman.gesture.exceptions.room.RoomNotFoundException;
import chainsawman.gesture.repository.call.CallParticipantRepository;
import chainsawman.gesture.repository.call.CallRepository;
import chainsawman.gesture.repository.room.RoomMemberRepository;
import chainsawman.gesture.repository.room.RoomRepository;
import chainsawman.gesture.security.SecurityUtils;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;

@Service
@RequiredArgsConstructor
public class CallService {

    private final CallRepository callRepository;
    private final CallParticipantRepository callParticipantRepository;
    private final RoomRepository roomRepository;
    private final RoomMemberRepository roomMemberRepository;
    private final SecurityUtils securityUtils;

    @Transactional
    public CallJoinResponse joinCall(Long roomIdx) {
        User currentUser = securityUtils.getCurrentUser();
        Room room = roomRepository.findById(roomIdx)
                .orElseThrow(RoomNotFoundException::new);

        if (!roomMemberRepository.existsByRoom_IdxAndUser_Idx(roomIdx, currentUser.getIdx())) {
            throw new RoomMemberNotFoundException();
        }

        // 활성 통화 세션이 없으면 새로 생성 (최초 참여자가 호스트)
        Call call = callRepository.findByRoom_IdxAndEndedAtIsNull(roomIdx)
                .orElseGet(() -> callRepository.save(Call.builder()
                        .room(room)
                        .startedBy(currentUser)
                        .build()));

        if (callParticipantRepository.existsByCall_IdxAndUser_IdxAndLeftAtIsNull(call.getIdx(), currentUser.getIdx())) {
            throw new CallAlreadyJoinedException();
        }

        CallParticipant participant = callParticipantRepository.save(CallParticipant.builder()
                .call(call)
                .user(currentUser)
                .build());

        int currentParticipant = callParticipantRepository.countByCall_IdxAndLeftAtIsNull(call.getIdx());

        return CallJoinResponse.builder()
                .callIdx(call.getIdx())
                .roomIdx(room.getIdx())
                .userIdx(currentUser.getIdx())
                .joinedAt(participant.getJoinedAt())
                .currentParticipant(currentParticipant)
                .maxParticipant(room.getMaxParticipant())
                .build();
    }

    @Transactional
    public CallLeaveResponse leaveCall(Long roomIdx) {
        User currentUser = securityUtils.getCurrentUser();
        Room room = roomRepository.findById(roomIdx)
                .orElseThrow(RoomNotFoundException::new);

        Call call = callRepository.findByRoom_IdxAndEndedAtIsNull(roomIdx)
                .orElseThrow(NoActiveCallException::new);

        CallParticipant participant = callParticipantRepository
                .findByCall_IdxAndUser_IdxAndLeftAtIsNull(call.getIdx(), currentUser.getIdx())
                .orElseThrow(CallParticipantNotFoundException::new);

        participant.setLeftAt(LocalDateTime.now());
        callParticipantRepository.save(participant);

        int remaining = callParticipantRepository.countByCall_IdxAndLeftAtIsNull(call.getIdx());

        boolean callEnded = remaining == 0;
        if (callEnded) {
            call.setEndedAt(LocalDateTime.now());
            callRepository.save(call);
        }

        return CallLeaveResponse.builder()
                .callIdx(call.getIdx())
                .roomIdx(room.getIdx())
                .userIdx(currentUser.getIdx())
                .leftAt(participant.getLeftAt())
                .currentParticipant(remaining)
                .callEnded(callEnded)
                .build();
    }

    @Transactional(readOnly = true)
    public CallParticipantsResponse getParticipants(Long roomIdx) {
        roomRepository.findById(roomIdx).orElseThrow(RoomNotFoundException::new);

        Call call = callRepository.findByRoom_IdxAndEndedAtIsNull(roomIdx)
                .orElseThrow(NoActiveCallException::new);

        Long hostIdx = call.getStartedBy().getIdx();

        List<CallParticipantInfo> participants = callParticipantRepository
                .findByCall_IdxAndLeftAtIsNullOrderByJoinedAtAsc(call.getIdx())
                .stream()
                .map(p -> CallParticipantInfo.builder()
                        .userIdx(p.getUser().getIdx())
                        .nickname(p.getUser().getNickname())
                        .joinedAt(p.getJoinedAt())
                        .isHost(p.getUser().getIdx().equals(hostIdx))
                        .build())
                .toList();

        return CallParticipantsResponse.builder()
                .callIdx(call.getIdx())
                .roomIdx(roomIdx)
                .participants(participants)
                .currentParticipant(participants.size())
                .build();
    }
}
