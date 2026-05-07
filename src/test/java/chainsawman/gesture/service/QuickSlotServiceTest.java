package chainsawman.gesture.service;

import chainsawman.gesture.dto.media.response.MediaUrlResponse;
import chainsawman.gesture.dto.quickSlot.request.PatchQuickSlotRequest;
import chainsawman.gesture.dto.quickSlot.request.QuickSlotRequest;
import chainsawman.gesture.dto.quickSlot.request.UpdateQuickSlotRequest;
import chainsawman.gesture.dto.quickSlot.response.*;
import chainsawman.gesture.entity.quick.QuickSlot;
import chainsawman.gesture.entity.quick.QuickSlotPreset;
import chainsawman.gesture.entity.user.User;
import chainsawman.gesture.exceptions.quickslot.QuickSlotLimitExceededException;
import chainsawman.gesture.exceptions.quickslot.QuickSlotNotFoundException;
import chainsawman.gesture.repository.quick.QuickSlotPresetRepository;
import chainsawman.gesture.repository.quick.QuickSlotRepository;
import chainsawman.gesture.security.SecurityUtils;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.util.ReflectionTestUtils;

import java.time.LocalDateTime;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class QuickSlotServiceTest {

    @Mock QuickSlotRepository quickSlotRepository;
    @Mock QuickSlotPresetRepository quickSlotPresetRepository;
    @Mock MediaService mediaService;
    @Mock SecurityUtils securityUtils;

    @InjectMocks QuickSlotService quickSlotService;

    private User user;

    @BeforeEach
    void setUp() {
        user = new User();
        ReflectionTestUtils.setField(user, "idx", 1L);
        given(securityUtils.getCurrentUser()).willReturn(user);
    }

    // ──────────────────────────────────────────────
    // createQuickSlot
    // ──────────────────────────────────────────────

    @Test
    @DisplayName("퀵슬롯 등록 - 정상 생성 및 응답 반환")
    void createQuickSlot_success() {
        QuickSlotRequest request = new QuickSlotRequest("제스처1", "설명", "uuid-icon-1");

        given(quickSlotRepository.countByUserAndDeletedAtIsNull(user)).willReturn(0L);
        given(quickSlotRepository.findOrdersByUser(user)).willReturn(Set.of(0, 1, 2));
        given(mediaService.getMediaUrl("uuid-icon-1")).willReturn(new MediaUrlResponse("https://s3.example.com/icon.png"));

        ArgumentCaptor<QuickSlot> captor = ArgumentCaptor.forClass(QuickSlot.class);
        given(quickSlotRepository.save(captor.capture())).willAnswer(inv -> {
            QuickSlot slot = inv.getArgument(0);
            ReflectionTestUtils.setField(slot, "idx", 10L);
            ReflectionTestUtils.setField(slot, "createdAt", LocalDateTime.now());
            return slot;
        });

        CreateQuickSlotResponse response = quickSlotService.createQuickSlot(request);

        assertThat(response.getName()).isEqualTo("제스처1");
        assertThat(response.getDescription()).isEqualTo("설명");
        assertThat(response.getIconUuid()).isEqualTo("uuid-icon-1");
        assertThat(response.getIconUrl()).isEqualTo("https://s3.example.com/icon.png");
        assertThat(response.getUserIdx()).isEqualTo(1L);

        QuickSlot saved = captor.getValue();
        assertThat(saved.getOrder()).isEqualTo(3); // 0,1,2 사용 중 → 다음은 3
        assertThat(saved.getUser()).isEqualTo(user);
    }

    @Test
    @DisplayName("퀵슬롯 등록 - 첫 번째 슬롯이면 order 0 할당")
    void createQuickSlot_firstSlot_orderZero() {
        given(quickSlotRepository.countByUserAndDeletedAtIsNull(user)).willReturn(0L);
        given(quickSlotRepository.findOrdersByUser(user)).willReturn(Set.of());
        given(mediaService.getMediaUrl(anyString())).willReturn(new MediaUrlResponse("https://s3.example.com/icon.png"));

        ArgumentCaptor<QuickSlot> captor = ArgumentCaptor.forClass(QuickSlot.class);
        given(quickSlotRepository.save(captor.capture())).willAnswer(inv -> {
            QuickSlot slot = inv.getArgument(0);
            ReflectionTestUtils.setField(slot, "idx", 1L);
            ReflectionTestUtils.setField(slot, "createdAt", LocalDateTime.now());
            return slot;
        });

        quickSlotService.createQuickSlot(new QuickSlotRequest("이름", null, "uuid"));

        assertThat(captor.getValue().getOrder()).isEqualTo(0);
    }

    @Test
    @DisplayName("퀵슬롯 등록 - 30개 초과 시 QuickSlotLimitExceededException")
    void createQuickSlot_limitExceeded() {
        given(quickSlotRepository.countByUserAndDeletedAtIsNull(user)).willReturn(30L);

        assertThatThrownBy(() -> quickSlotService.createQuickSlot(new QuickSlotRequest("이름", null, "uuid")))
                .isInstanceOf(QuickSlotLimitExceededException.class)
                .hasMessageContaining("30");

        verify(quickSlotRepository, never()).save(any());
    }

    // ──────────────────────────────────────────────
    // updateQuickSlots
    // ──────────────────────────────────────────────

    @Test
    @DisplayName("활성 프리셋 등록 - 정상 업데이트, 기존 프리셋 재사용")
    void updateQuickSlots_existingPreset() {
        List<Long> ids = List.of(1L, 2L, 3L);
        List<QuickSlot> slots = makeSlots(1L, 2L, 3L);
        QuickSlotPreset preset = QuickSlotPreset.create(user);
        LocalDateTime updatedAt = LocalDateTime.now();

        given(quickSlotRepository.findByIdxInAndUserAndDeletedAtIsNull(ids, user)).willReturn(slots);
        given(quickSlotPresetRepository.findByUser(user)).willReturn(Optional.of(preset));
        given(quickSlotPresetRepository.save(preset)).willAnswer(inv -> {
            ReflectionTestUtils.setField(preset, "updatedAt", updatedAt);
            return preset;
        });

        UpdateQuickSlotResponse response = quickSlotService.updateQuickSlots(new UpdateQuickSlotRequest(ids));

        assertThat(response.getUserIdx()).isEqualTo(1L);
        assertThat(response.getQuickSlots()).hasSize(3);
        assertThat(response.getQuickSlots().get(0).getOrder()).isEqualTo(1);
        assertThat(response.getQuickSlots().get(2).getOrder()).isEqualTo(3);
        assertThat(response.getUpdatedAt()).isEqualTo(updatedAt);
    }

    @Test
    @DisplayName("활성 프리셋 등록 - 프리셋 없으면 새로 생성")
    void updateQuickSlots_createsNewPreset() {
        List<Long> ids = List.of(1L);
        List<QuickSlot> slots = makeSlots(1L);

        given(quickSlotRepository.findByIdxInAndUserAndDeletedAtIsNull(ids, user)).willReturn(slots);
        given(quickSlotPresetRepository.findByUser(user)).willReturn(Optional.empty());
        given(quickSlotPresetRepository.save(any())).willAnswer(inv -> {
            QuickSlotPreset p = inv.getArgument(0);
            ReflectionTestUtils.setField(p, "updatedAt", LocalDateTime.now());
            return p;
        });

        UpdateQuickSlotResponse response = quickSlotService.updateQuickSlots(new UpdateQuickSlotRequest(ids));

        assertThat(response.getQuickSlots()).hasSize(1);
        verify(quickSlotPresetRepository).save(any(QuickSlotPreset.class));
    }

    @Test
    @DisplayName("활성 프리셋 등록 - 존재하지 않는 슬롯 ID 포함 시 QuickSlotNotFoundException")
    void updateQuickSlots_slotNotFound() {
        List<Long> ids = List.of(1L, 2L, 99L);
        given(quickSlotRepository.findByIdxInAndUserAndDeletedAtIsNull(ids, user))
                .willReturn(makeSlots(1L, 2L)); // 99L 없음

        assertThatThrownBy(() -> quickSlotService.updateQuickSlots(new UpdateQuickSlotRequest(ids)))
                .isInstanceOf(QuickSlotNotFoundException.class);

        verify(quickSlotPresetRepository, never()).save(any());
    }

    // ──────────────────────────────────────────────
    // getQuickSlots
    // ──────────────────────────────────────────────

    @Test
    @DisplayName("퀵슬롯 목록 조회 - 아이콘 URL 포함 반환")
    void getQuickSlots_withIcons() {
        List<QuickSlot> slots = makeSlots(1L, 2L);
        ReflectionTestUtils.setField(slots.get(0), "iconUuid", "uuid-a");
        ReflectionTestUtils.setField(slots.get(1), "iconUuid", "uuid-b");

        given(quickSlotRepository.findByUserAndDeletedAtIsNullOrderByOrder(user)).willReturn(slots);
        given(mediaService.getMediaUrlMap(anySet())).willReturn(Map.of(
                "uuid-a", "https://s3.example.com/a.png",
                "uuid-b", "https://s3.example.com/b.png"
        ));

        List<QuickSlotListResponse> result = quickSlotService.getQuickSlots();

        assertThat(result).hasSize(2);
        assertThat(result.get(0).getIconUrl()).isEqualTo("https://s3.example.com/a.png");
        assertThat(result.get(1).getIconUrl()).isEqualTo("https://s3.example.com/b.png");
    }

    @Test
    @DisplayName("퀵슬롯 목록 조회 - 슬롯 없으면 빈 리스트 반환")
    void getQuickSlots_empty() {
        given(quickSlotRepository.findByUserAndDeletedAtIsNullOrderByOrder(user)).willReturn(List.of());
        given(mediaService.getMediaUrlMap(anySet())).willReturn(Map.of());

        List<QuickSlotListResponse> result = quickSlotService.getQuickSlots();

        assertThat(result).isEmpty();
    }

    // ──────────────────────────────────────────────
    // getPreset
    // ──────────────────────────────────────────────

    @Test
    @DisplayName("프리셋 조회 - 활성 슬롯과 아이콘 URL 반환")
    void getPreset_withActiveSlots() {
        QuickSlotPreset preset = QuickSlotPreset.create(user);
        QuickSlot slot = makeSlot(10L, "uuid-x");
        preset.updateSlots(List.of(slot));
        LocalDateTime updatedAt = LocalDateTime.now();
        ReflectionTestUtils.setField(preset, "updatedAt", updatedAt);

        given(quickSlotPresetRepository.findByUser(user)).willReturn(Optional.of(preset));
        given(mediaService.getMediaUrlMap(Set.of("uuid-x"))).willReturn(Map.of("uuid-x", "https://s3.example.com/x.png"));

        QuickSlotPresetResponse response = quickSlotService.getPreset();

        assertThat(response.getUserIdx()).isEqualTo(1L);
        assertThat(response.getQuickSlots()).hasSize(1);
        assertThat(response.getQuickSlots().get(0).getIconUrl()).isEqualTo("https://s3.example.com/x.png");
        assertThat(response.getUpdatedAt()).isEqualTo(updatedAt);
    }

    @Test
    @DisplayName("프리셋 조회 - 프리셋 없으면 빈 슬롯 응답 반환")
    void getPreset_noPreset() {
        given(quickSlotPresetRepository.findByUser(user)).willReturn(Optional.empty());

        QuickSlotPresetResponse response = quickSlotService.getPreset();

        assertThat(response.getUserIdx()).isEqualTo(1L);
        assertThat(response.getQuickSlots()).isEmpty();
    }

    // ──────────────────────────────────────────────
    // patchQuickSlot
    // ──────────────────────────────────────────────

    @Test
    @DisplayName("퀵슬롯 수정 - 이름/설명/아이콘 변경 후 반환")
    void patchQuickSlot_success() {
        QuickSlot slot = makeSlot(5L, "old-uuid");
        PatchQuickSlotRequest request = new PatchQuickSlotRequest("새이름", "새설명", "new-uuid");

        given(quickSlotRepository.findByIdxAndUserAndDeletedAtIsNull(5L, user)).willReturn(Optional.of(slot));
        given(mediaService.getMediaUrl("new-uuid")).willReturn(new MediaUrlResponse("https://s3.example.com/new.png"));

        QuickSlotListResponse response = quickSlotService.patchQuickSlot(5L, request);

        assertThat(response.getName()).isEqualTo("새이름");
        assertThat(response.getDescription()).isEqualTo("새설명");
        assertThat(response.getIconUuid()).isEqualTo("new-uuid");
        assertThat(response.getIconUrl()).isEqualTo("https://s3.example.com/new.png");
    }

    @Test
    @DisplayName("퀵슬롯 수정 - null 필드는 기존 값 유지")
    void patchQuickSlot_partialUpdate() {
        QuickSlot slot = makeSlot(5L, "old-uuid");
        ReflectionTestUtils.setField(slot, "name", "기존이름");
        PatchQuickSlotRequest request = new PatchQuickSlotRequest(null, "새설명", null);

        given(quickSlotRepository.findByIdxAndUserAndDeletedAtIsNull(5L, user)).willReturn(Optional.of(slot));
        given(mediaService.getMediaUrl("old-uuid")).willReturn(new MediaUrlResponse("https://s3.example.com/old.png"));

        QuickSlotListResponse response = quickSlotService.patchQuickSlot(5L, request);

        assertThat(response.getName()).isEqualTo("기존이름");
        assertThat(response.getDescription()).isEqualTo("새설명");
        assertThat(response.getIconUuid()).isEqualTo("old-uuid");
    }

    @Test
    @DisplayName("퀵슬롯 수정 - 존재하지 않는 슬롯이면 QuickSlotNotFoundException")
    void patchQuickSlot_notFound() {
        given(quickSlotRepository.findByIdxAndUserAndDeletedAtIsNull(99L, user)).willReturn(Optional.empty());

        assertThatThrownBy(() -> quickSlotService.patchQuickSlot(99L, new PatchQuickSlotRequest(null, null, null)))
                .isInstanceOf(QuickSlotNotFoundException.class);
    }

    // ──────────────────────────────────────────────
    // deleteQuickSlot
    // ──────────────────────────────────────────────

    @Test
    @DisplayName("퀵슬롯 삭제 - soft delete 처리 및 응답 반환")
    void deleteQuickSlot_success() {
        QuickSlot slot = makeSlot(7L, "uuid-del");

        given(quickSlotRepository.findByIdxAndUserAndDeletedAtIsNull(7L, user)).willReturn(Optional.of(slot));
        given(quickSlotPresetRepository.findByUser(user)).willReturn(Optional.empty());

        DeleteQuickSlotResponse response = quickSlotService.deleteQuickSlot(7L);

        assertThat(response.getQuickSlotIdx()).isEqualTo(7L);
        assertThat(response.isDeleted()).isTrue();
        assertThat(slot.getDeletedAt()).isNotNull();
    }

    @Test
    @DisplayName("퀵슬롯 삭제 - 프리셋에 포함된 슬롯이면 프리셋에서도 제거")
    void deleteQuickSlot_removesFromPreset() {
        QuickSlot slot = makeSlot(7L, "uuid-del");
        QuickSlotPreset preset = QuickSlotPreset.create(user);
        preset.updateSlots(List.of(slot));

        given(quickSlotRepository.findByIdxAndUserAndDeletedAtIsNull(7L, user)).willReturn(Optional.of(slot));
        given(quickSlotPresetRepository.findByUser(user)).willReturn(Optional.of(preset));

        quickSlotService.deleteQuickSlot(7L);

        assertThat(preset.getActiveSlots()).isEmpty();
    }

    @Test
    @DisplayName("퀵슬롯 삭제 - 존재하지 않는 슬롯이면 QuickSlotNotFoundException")
    void deleteQuickSlot_notFound() {
        given(quickSlotRepository.findByIdxAndUserAndDeletedAtIsNull(99L, user)).willReturn(Optional.empty());

        assertThatThrownBy(() -> quickSlotService.deleteQuickSlot(99L))
                .isInstanceOf(QuickSlotNotFoundException.class);
    }

    // ──────────────────────────────────────────────
    // helpers
    // ──────────────────────────────────────────────

    private QuickSlot makeSlot(Long idx, String iconUuid) {
        QuickSlot slot = QuickSlot.builder()
                .user(user)
                .name("슬롯" + idx)
                .description("설명" + idx)
                .iconUuid(iconUuid)
                .order(idx.intValue())
                .build();
        ReflectionTestUtils.setField(slot, "idx", idx);
        ReflectionTestUtils.setField(slot, "createdAt", LocalDateTime.now());
        return slot;
    }

    private List<QuickSlot> makeSlots(Long... idxs) {
        return java.util.Arrays.stream(idxs)
                .map(i -> makeSlot(i, "uuid-" + i))
                .toList();
    }
}
