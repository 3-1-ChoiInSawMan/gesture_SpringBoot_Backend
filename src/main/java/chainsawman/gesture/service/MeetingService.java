package chainsawman.gesture.service;

import chainsawman.gesture.dto.meeting.request.MeetingMinutesCreateRequest;
import chainsawman.gesture.dto.meeting.request.MeetingMinutesUpdateRequest;
import chainsawman.gesture.dto.meeting.response.*;
import chainsawman.gesture.entity.call.Call;
import chainsawman.gesture.entity.meeting.Meeting;
import chainsawman.gesture.entity.user.User;
import chainsawman.gesture.enums.MeetingStatus;
import chainsawman.gesture.exceptions.call.CallNotFoundException;
import chainsawman.gesture.exceptions.meeting.MeetingAlreadyStartedException;
import chainsawman.gesture.exceptions.meeting.MeetingNotFoundException;
import chainsawman.gesture.exceptions.meeting.MeetingNotInProgressException;
import chainsawman.gesture.exceptions.room.RoomNotFoundException;
import chainsawman.gesture.repository.call.CallRepository;
import chainsawman.gesture.repository.meeting.MeetingRepository;
import chainsawman.gesture.repository.room.RoomRepository;
import chainsawman.gesture.security.SecurityUtils;
import lombok.RequiredArgsConstructor;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;

@Service
@RequiredArgsConstructor
public class MeetingService {

    private final MeetingRepository meetingRepository;
    private final CallRepository callRepository;
    private final RoomRepository roomRepository;
    private final SecurityUtils securityUtils;

    @Transactional
    public MeetingStartResponse startMinutes(Long callIdx) {
        User currentUser = securityUtils.getCurrentUser();
        Call call = callRepository.findById(callIdx)
                .orElseThrow(CallNotFoundException::new);

        if (meetingRepository.existsByCall_IdxAndStatus(callIdx, MeetingStatus.IN_PROGRESS)) {
            throw new MeetingAlreadyStartedException();
        }

        Meeting meeting = meetingRepository.save(Meeting.builder()
                .call(call)
                .createdBy(currentUser)
                .status(MeetingStatus.IN_PROGRESS)
                .startedAt(LocalDateTime.now())
                .build());

        return MeetingStartResponse.builder()
                .minutesIdx(meeting.getIdx())
                .callIdx(call.getIdx())
                .roomIdx(call.getRoom().getIdx())
                .startedAt(meeting.getStartedAt())
                .status(meeting.getStatus().name())
                .build();
    }

    @Transactional
    public MeetingDetailResponse createMinutes(Long callIdx, MeetingMinutesCreateRequest request) {
        callRepository.findById(callIdx).orElseThrow(CallNotFoundException::new);

        Meeting meeting = meetingRepository.findByCall_IdxAndStatus(callIdx, MeetingStatus.IN_PROGRESS)
                .orElseThrow(MeetingNotInProgressException::new);

        if (request.getTitle() != null) meeting.setTitle(request.getTitle());
        if (request.getTranscript() != null) meeting.setContent(String.join("\n", request.getTranscript()));
        if (request.getParticipants() != null) meeting.setParticipants(request.getParticipants());
        if (request.getAiSummary() != null) meeting.setAiSummary(request.getAiSummary());
        if (request.getConclusion() != null) meeting.setConclusion(request.getConclusion());

        meetingRepository.save(meeting);

        return toDetailResponse(meeting);
    }

    @Transactional
    public MeetingEndResponse endMinutes(Long callIdx) {
        callRepository.findById(callIdx).orElseThrow(CallNotFoundException::new);

        Meeting meeting = meetingRepository.findByCall_IdxAndStatus(callIdx, MeetingStatus.IN_PROGRESS)
                .orElseThrow(MeetingNotInProgressException::new);

        meeting.setStatus(MeetingStatus.ENDED);
        meeting.setEndedAt(LocalDateTime.now());
        meetingRepository.save(meeting);

        Call call = meeting.getCall();
        return MeetingEndResponse.builder()
                .minutesIdx(meeting.getIdx())
                .callIdx(call.getIdx())
                .roomIdx(call.getRoom().getIdx())
                .title(meeting.getTitle())
                .meetingDate(meeting.getStartedAt())
                .participants(meeting.getParticipants())
                .aiSummary(meeting.getAiSummary())
                .conclusion(meeting.getConclusion())
                .endedAt(meeting.getEndedAt())
                .build();
    }

    @Transactional(readOnly = true)
    public List<MeetingListItemResponse> getMinutesList(Long roomIdx) {
        roomRepository.findById(roomIdx).orElseThrow(RoomNotFoundException::new);

        return meetingRepository.findByCall_Room_IdxOrderByStartedAtDesc(roomIdx).stream()
                .map(m -> MeetingListItemResponse.builder()
                        .minutesIdx(m.getIdx())
                        .callIdx(m.getCall().getIdx())
                        .title(m.getTitle())
                        .meetingDate(m.getStartedAt())
                        .status(m.getStatus().name())
                        .build())
                .toList();
    }

    @Transactional(readOnly = true)
    public MeetingDetailResponse getMinutesDetail(Long minutesIdx) {
        Meeting meeting = meetingRepository.findById(minutesIdx)
                .orElseThrow(MeetingNotFoundException::new);

        return toDetailResponse(meeting);
    }

    @Transactional
    public MeetingDetailResponse updateMinutes(Long minutesIdx, MeetingMinutesUpdateRequest request) {
        User currentUser = securityUtils.getCurrentUser();

        Meeting meeting = meetingRepository.findById(minutesIdx)
                .orElseThrow(MeetingNotFoundException::new);

        if (!meeting.getCreatedBy().getIdx().equals(currentUser.getIdx())) {
            throw new AccessDeniedException("본인이 시작한 회의록만 수정할 수 있습니다.");
        }

        if (request.getTitle() != null) meeting.setTitle(request.getTitle());
        if (request.getContent() != null) meeting.setContent(request.getContent());
        if (request.getConclusion() != null) meeting.setConclusion(request.getConclusion());

        meetingRepository.save(meeting);

        return toDetailResponse(meeting);
    }

    @Transactional
    public void deleteMinutes(Long minutesIdx) {
        User currentUser = securityUtils.getCurrentUser();

        Meeting meeting = meetingRepository.findById(minutesIdx)
                .orElseThrow(MeetingNotFoundException::new);

        if (!meeting.getCreatedBy().getIdx().equals(currentUser.getIdx())) {
            throw new AccessDeniedException("본인이 시작한 회의록만 삭제할 수 있습니다.");
        }

        meetingRepository.delete(meeting);
    }

    private MeetingDetailResponse toDetailResponse(Meeting meeting) {
        Call call = meeting.getCall();
        return MeetingDetailResponse.builder()
                .minutesIdx(meeting.getIdx())
                .callIdx(call.getIdx())
                .roomIdx(call.getRoom().getIdx())
                .title(meeting.getTitle())
                .meetingDate(meeting.getStartedAt())
                .participants(meeting.getParticipants())
                .content(meeting.getContent())
                .aiSummary(meeting.getAiSummary())
                .conclusion(meeting.getConclusion())
                .status(meeting.getStatus().name())
                .startedAt(meeting.getStartedAt())
                .endedAt(meeting.getEndedAt())
                .build();
    }
}
