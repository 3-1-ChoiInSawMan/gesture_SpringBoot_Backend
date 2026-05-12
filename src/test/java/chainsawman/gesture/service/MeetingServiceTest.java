package chainsawman.gesture.service;

import chainsawman.gesture.dto.meeting.request.MeetingMinutesCreateRequest;
import chainsawman.gesture.dto.meeting.request.MeetingMinutesUpdateRequest;
import chainsawman.gesture.dto.meeting.response.*;
import chainsawman.gesture.entity.call.Call;
import chainsawman.gesture.entity.meeting.Meeting;
import chainsawman.gesture.entity.room.Room;
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
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.mockito.junit.jupiter.MockitoSettings;
import org.mockito.quality.Strictness;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.test.util.ReflectionTestUtils;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;

@ExtendWith(MockitoExtension.class)
@MockitoSettings(strictness = Strictness.LENIENT)
class MeetingServiceTest {

    @Mock MeetingRepository meetingRepository;
    @Mock CallRepository callRepository;
    @Mock RoomRepository roomRepository;
    @Mock SecurityUtils securityUtils;

    @InjectMocks MeetingService meetingService;

    private User user;
    private Room room;
    private Call call;

    @BeforeEach
    void setUp() {
        user = new User();
        ReflectionTestUtils.setField(user, "idx", 1L);

        room = Room.builder().host(user).title("테스트방").maxParticipant(5).build();
        ReflectionTestUtils.setField(room, "idx", 10L);

        call = Call.builder().room(room).startedBy(user).build();
        ReflectionTestUtils.setField(call, "idx", 100L);

        given(securityUtils.getCurrentUser()).willReturn(user);
    }

    // ─── startMinutes ─────────────────────────────

    @Test
    @DisplayName("회의록 시작 - IN_PROGRESS 회의록 생성, callIdx·roomIdx 응답에 포함")
    void startMinutes_success() {
        given(callRepository.findById(100L)).willReturn(Optional.of(call));
        given(meetingRepository.existsByCall_IdxAndStatus(100L, MeetingStatus.IN_PROGRESS)).willReturn(false);
        given(meetingRepository.save(any(Meeting.class))).willAnswer(inv -> {
            Meeting m = inv.getArgument(0);
            ReflectionTestUtils.setField(m, "idx", 1L);
            return m;
        });

        MeetingStartResponse response = meetingService.startMinutes(100L);

        assertThat(response.getMinutesIdx()).isEqualTo(1L);
        assertThat(response.getCallIdx()).isEqualTo(100L);
        assertThat(response.getRoomIdx()).isEqualTo(10L);
        assertThat(response.getStatus()).isEqualTo("IN_PROGRESS");
        assertThat(response.getStartedAt()).isNotNull();
    }

    @Test
    @DisplayName("회의록 시작 - 존재하지 않는 통화 세션이면 CallNotFoundException")
    void startMinutes_call_not_found() {
        given(callRepository.findById(999L)).willReturn(Optional.empty());

        assertThatThrownBy(() -> meetingService.startMinutes(999L))
                .isInstanceOf(CallNotFoundException.class);

        verify(meetingRepository, never()).save(any());
    }

    @Test
    @DisplayName("회의록 시작 - 이미 진행 중인 회의록이 있으면 MeetingAlreadyStartedException")
    void startMinutes_already_in_progress() {
        given(callRepository.findById(100L)).willReturn(Optional.of(call));
        given(meetingRepository.existsByCall_IdxAndStatus(100L, MeetingStatus.IN_PROGRESS)).willReturn(true);

        assertThatThrownBy(() -> meetingService.startMinutes(100L))
                .isInstanceOf(MeetingAlreadyStartedException.class);

        verify(meetingRepository, never()).save(any());
    }

    // ─── createMinutes ────────────────────────────

    @Test
    @DisplayName("회의록 생성 - transcript는 줄바꿈으로 합쳐 content에 저장")
    void createMinutes_transcript_joined() {
        Meeting meeting = buildInProgressMeeting(1L);

        given(callRepository.findById(100L)).willReturn(Optional.of(call));
        given(meetingRepository.findByCall_IdxAndStatus(100L, MeetingStatus.IN_PROGRESS))
                .willReturn(Optional.of(meeting));
        given(meetingRepository.save(any())).willAnswer(inv -> inv.getArgument(0));

        MeetingMinutesCreateRequest request = buildCreateRequest(
                "프로젝트 회의",
                List.of("안녕하세요", "회의 시작합니다"),
                List.of("윤정", "현우"),
                "프로젝트 일정 논의",
                List.of("개발 완료 목표 6월")
        );

        MeetingDetailResponse response = meetingService.createMinutes(100L, request);

        assertThat(response.getContent()).isEqualTo("안녕하세요\n회의 시작합니다");
        assertThat(response.getCallIdx()).isEqualTo(100L);
        assertThat(response.getRoomIdx()).isEqualTo(10L);
        assertThat(response.getParticipants()).containsExactly("윤정", "현우");
        assertThat(response.getAiSummary()).isEqualTo("프로젝트 일정 논의");
        assertThat(response.getConclusion()).containsExactly("개발 완료 목표 6월");
    }

    @Test
    @DisplayName("회의록 생성 - null 필드는 기존 값 유지")
    void createMinutes_null_fields_preserved() {
        Meeting meeting = buildInProgressMeeting(1L);
        meeting.setTitle("기존 제목");
        meeting.setContent("기존 내용");

        given(callRepository.findById(100L)).willReturn(Optional.of(call));
        given(meetingRepository.findByCall_IdxAndStatus(100L, MeetingStatus.IN_PROGRESS))
                .willReturn(Optional.of(meeting));
        given(meetingRepository.save(any())).willAnswer(inv -> inv.getArgument(0));

        MeetingDetailResponse response = meetingService.createMinutes(100L, buildCreateRequest(null, null, null, null, null));

        assertThat(response.getTitle()).isEqualTo("기존 제목");
        assertThat(response.getContent()).isEqualTo("기존 내용");
    }

    @Test
    @DisplayName("회의록 생성 - 진행 중인 회의록 없으면 MeetingNotInProgressException")
    void createMinutes_not_in_progress() {
        given(callRepository.findById(100L)).willReturn(Optional.of(call));
        given(meetingRepository.findByCall_IdxAndStatus(100L, MeetingStatus.IN_PROGRESS))
                .willReturn(Optional.empty());

        assertThatThrownBy(() -> meetingService.createMinutes(100L, new MeetingMinutesCreateRequest()))
                .isInstanceOf(MeetingNotInProgressException.class);
    }

    @Test
    @DisplayName("회의록 생성 - 존재하지 않는 통화 세션이면 CallNotFoundException")
    void createMinutes_call_not_found() {
        given(callRepository.findById(999L)).willReturn(Optional.empty());

        assertThatThrownBy(() -> meetingService.createMinutes(999L, new MeetingMinutesCreateRequest()))
                .isInstanceOf(CallNotFoundException.class);
    }

    // ─── endMinutes ───────────────────────────────

    @Test
    @DisplayName("회의록 종료 - 상태 ENDED, ended_at 설정, callIdx·roomIdx 응답에 포함")
    void endMinutes_success() {
        Meeting meeting = buildInProgressMeeting(1L);
        meeting.setTitle("프로젝트 회의");
        meeting.setParticipants(List.of("윤정", "현우"));
        meeting.setAiSummary("프로젝트 일정 논의");
        meeting.setConclusion(List.of("개발 완료 목표 6월"));

        given(callRepository.findById(100L)).willReturn(Optional.of(call));
        given(meetingRepository.findByCall_IdxAndStatus(100L, MeetingStatus.IN_PROGRESS))
                .willReturn(Optional.of(meeting));
        given(meetingRepository.save(any())).willAnswer(inv -> inv.getArgument(0));

        MeetingEndResponse response = meetingService.endMinutes(100L);

        assertThat(meeting.getStatus()).isEqualTo(MeetingStatus.ENDED);
        assertThat(meeting.getEndedAt()).isNotNull();
        assertThat(response.getCallIdx()).isEqualTo(100L);
        assertThat(response.getRoomIdx()).isEqualTo(10L);
        assertThat(response.getEndedAt()).isNotNull();
    }

    @Test
    @DisplayName("회의록 종료 - 진행 중인 회의록 없으면 MeetingNotInProgressException")
    void endMinutes_not_in_progress() {
        given(callRepository.findById(100L)).willReturn(Optional.of(call));
        given(meetingRepository.findByCall_IdxAndStatus(100L, MeetingStatus.IN_PROGRESS))
                .willReturn(Optional.empty());

        assertThatThrownBy(() -> meetingService.endMinutes(100L))
                .isInstanceOf(MeetingNotInProgressException.class);
    }

    @Test
    @DisplayName("회의록 종료 - 존재하지 않는 통화 세션이면 CallNotFoundException")
    void endMinutes_call_not_found() {
        given(callRepository.findById(999L)).willReturn(Optional.empty());

        assertThatThrownBy(() -> meetingService.endMinutes(999L))
                .isInstanceOf(CallNotFoundException.class);
    }

    // ─── getMinutesList ───────────────────────────

    @Test
    @DisplayName("회의록 목록 조회 - 통화방 기준으로 callIdx 포함해 반환")
    void getMinutesList_success() {
        Meeting m1 = buildEndedMeeting(1L, "첫 번째 회의");
        Meeting m2 = buildEndedMeeting(2L, "두 번째 회의");

        given(roomRepository.findById(10L)).willReturn(Optional.of(room));
        given(meetingRepository.findByCall_Room_IdxOrderByStartedAtDesc(10L)).willReturn(List.of(m1, m2));

        List<MeetingListItemResponse> result = meetingService.getMinutesList(10L);

        assertThat(result).hasSize(2);
        assertThat(result.get(0).getMinutesIdx()).isEqualTo(1L);
        assertThat(result.get(0).getCallIdx()).isEqualTo(100L);
        assertThat(result.get(0).getStatus()).isEqualTo("ENDED");
    }

    @Test
    @DisplayName("회의록 목록 조회 - 빈 목록이면 빈 리스트 반환")
    void getMinutesList_empty() {
        given(roomRepository.findById(10L)).willReturn(Optional.of(room));
        given(meetingRepository.findByCall_Room_IdxOrderByStartedAtDesc(10L)).willReturn(List.of());

        assertThat(meetingService.getMinutesList(10L)).isEmpty();
    }

    @Test
    @DisplayName("회의록 목록 조회 - 존재하지 않는 방이면 RoomNotFoundException")
    void getMinutesList_room_not_found() {
        given(roomRepository.findById(99L)).willReturn(Optional.empty());

        assertThatThrownBy(() -> meetingService.getMinutesList(99L))
                .isInstanceOf(RoomNotFoundException.class);
    }

    // ─── getMinutesDetail ─────────────────────────

    @Test
    @DisplayName("회의록 단건 조회 - callIdx·roomIdx 포함한 전체 데이터 반환")
    void getMinutesDetail_success() {
        Meeting meeting = buildEndedMeeting(1L, "프로젝트 회의");
        meeting.setContent("회의 내용");
        meeting.setParticipants(List.of("윤정", "현우"));
        meeting.setAiSummary("AI 요약");
        meeting.setConclusion(List.of("결론 1", "결론 2"));

        given(meetingRepository.findById(1L)).willReturn(Optional.of(meeting));

        MeetingDetailResponse response = meetingService.getMinutesDetail(1L);

        assertThat(response.getMinutesIdx()).isEqualTo(1L);
        assertThat(response.getCallIdx()).isEqualTo(100L);
        assertThat(response.getRoomIdx()).isEqualTo(10L);
        assertThat(response.getTitle()).isEqualTo("프로젝트 회의");
        assertThat(response.getParticipants()).containsExactly("윤정", "현우");
        assertThat(response.getAiSummary()).isEqualTo("AI 요약");
    }

    @Test
    @DisplayName("회의록 단건 조회 - 존재하지 않는 회의록이면 MeetingNotFoundException")
    void getMinutesDetail_not_found() {
        given(meetingRepository.findById(99L)).willReturn(Optional.empty());

        assertThatThrownBy(() -> meetingService.getMinutesDetail(99L))
                .isInstanceOf(MeetingNotFoundException.class);
    }

    // ─── updateMinutes ────────────────────────────

    @Test
    @DisplayName("회의록 수정 - 제목, 내용, 결론 정상 수정")
    void updateMinutes_success() {
        Meeting meeting = buildEndedMeeting(1L, "원래 제목");
        meeting.setContent("원래 내용");
        meeting.setConclusion(new ArrayList<>(List.of("원래 결론")));
        ReflectionTestUtils.setField(meeting, "createdBy", user);

        given(meetingRepository.findById(1L)).willReturn(Optional.of(meeting));
        given(meetingRepository.save(any())).willAnswer(inv -> inv.getArgument(0));

        MeetingDetailResponse response = meetingService.updateMinutes(1L,
                buildUpdateRequest("수정 제목", "수정 내용", List.of("새 결론 1", "새 결론 2")));

        assertThat(response.getTitle()).isEqualTo("수정 제목");
        assertThat(response.getContent()).isEqualTo("수정 내용");
        assertThat(response.getConclusion()).containsExactly("새 결론 1", "새 결론 2");
    }

    @Test
    @DisplayName("회의록 수정 - null 필드는 기존 값 유지")
    void updateMinutes_null_fields_preserved() {
        Meeting meeting = buildEndedMeeting(1L, "원래 제목");
        meeting.setContent("원래 내용");
        ReflectionTestUtils.setField(meeting, "createdBy", user);

        given(meetingRepository.findById(1L)).willReturn(Optional.of(meeting));
        given(meetingRepository.save(any())).willAnswer(inv -> inv.getArgument(0));

        MeetingDetailResponse response = meetingService.updateMinutes(1L, buildUpdateRequest(null, null, null));

        assertThat(response.getTitle()).isEqualTo("원래 제목");
        assertThat(response.getContent()).isEqualTo("원래 내용");
    }

    @Test
    @DisplayName("회의록 수정 - 본인이 시작하지 않은 회의록이면 AccessDeniedException")
    void updateMinutes_access_denied() {
        User otherUser = new User();
        ReflectionTestUtils.setField(otherUser, "idx", 2L);

        Meeting meeting = buildEndedMeeting(1L, "회의");
        ReflectionTestUtils.setField(meeting, "createdBy", otherUser);

        given(meetingRepository.findById(1L)).willReturn(Optional.of(meeting));

        assertThatThrownBy(() -> meetingService.updateMinutes(1L, buildUpdateRequest("제목", null, null)))
                .isInstanceOf(AccessDeniedException.class);

        verify(meetingRepository, never()).save(any());
    }

    @Test
    @DisplayName("회의록 수정 - 존재하지 않는 회의록이면 MeetingNotFoundException")
    void updateMinutes_not_found() {
        given(meetingRepository.findById(99L)).willReturn(Optional.empty());

        assertThatThrownBy(() -> meetingService.updateMinutes(99L, buildUpdateRequest("제목", null, null)))
                .isInstanceOf(MeetingNotFoundException.class);
    }

    // ─── deleteMinutes ────────────────────────────

    @Test
    @DisplayName("회의록 삭제 - 정상 삭제")
    void deleteMinutes_success() {
        Meeting meeting = buildEndedMeeting(1L, "회의");
        ReflectionTestUtils.setField(meeting, "createdBy", user);

        given(meetingRepository.findById(1L)).willReturn(Optional.of(meeting));

        meetingService.deleteMinutes(1L);

        verify(meetingRepository).delete(meeting);
    }

    @Test
    @DisplayName("회의록 삭제 - 본인이 시작하지 않은 회의록이면 AccessDeniedException")
    void deleteMinutes_access_denied() {
        User otherUser = new User();
        ReflectionTestUtils.setField(otherUser, "idx", 2L);

        Meeting meeting = buildEndedMeeting(1L, "회의");
        ReflectionTestUtils.setField(meeting, "createdBy", otherUser);

        given(meetingRepository.findById(1L)).willReturn(Optional.of(meeting));

        assertThatThrownBy(() -> meetingService.deleteMinutes(1L))
                .isInstanceOf(AccessDeniedException.class);

        verify(meetingRepository, never()).delete(any());
    }

    @Test
    @DisplayName("회의록 삭제 - 존재하지 않는 회의록이면 MeetingNotFoundException")
    void deleteMinutes_not_found() {
        given(meetingRepository.findById(99L)).willReturn(Optional.empty());

        assertThatThrownBy(() -> meetingService.deleteMinutes(99L))
                .isInstanceOf(MeetingNotFoundException.class);

        verify(meetingRepository, never()).delete(any());
    }

    // ─── helpers ──────────────────────────────────

    private Meeting buildInProgressMeeting(Long idx) {
        Meeting meeting = Meeting.builder()
                .call(call)
                .createdBy(user)
                .status(MeetingStatus.IN_PROGRESS)
                .startedAt(LocalDateTime.now())
                .build();
        ReflectionTestUtils.setField(meeting, "idx", idx);
        return meeting;
    }

    private Meeting buildEndedMeeting(Long idx, String title) {
        Meeting meeting = Meeting.builder()
                .call(call)
                .createdBy(user)
                .title(title)
                .status(MeetingStatus.ENDED)
                .startedAt(LocalDateTime.now().minusHours(1))
                .endedAt(LocalDateTime.now())
                .build();
        ReflectionTestUtils.setField(meeting, "idx", idx);
        return meeting;
    }

    private MeetingMinutesCreateRequest buildCreateRequest(
            String title, List<String> transcript, List<String> participants,
            String aiSummary, List<String> conclusion) {
        MeetingMinutesCreateRequest req = new MeetingMinutesCreateRequest();
        ReflectionTestUtils.setField(req, "title", title);
        ReflectionTestUtils.setField(req, "transcript", transcript);
        ReflectionTestUtils.setField(req, "participants", participants);
        ReflectionTestUtils.setField(req, "aiSummary", aiSummary);
        ReflectionTestUtils.setField(req, "conclusion", conclusion);
        return req;
    }

    private MeetingMinutesUpdateRequest buildUpdateRequest(String title, String content, List<String> conclusion) {
        MeetingMinutesUpdateRequest req = new MeetingMinutesUpdateRequest();
        ReflectionTestUtils.setField(req, "title", title);
        ReflectionTestUtils.setField(req, "content", content);
        ReflectionTestUtils.setField(req, "conclusion", conclusion);
        return req;
    }
}
