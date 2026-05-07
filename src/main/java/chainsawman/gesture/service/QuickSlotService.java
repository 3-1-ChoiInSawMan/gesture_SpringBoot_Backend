package chainsawman.gesture.service;

import chainsawman.gesture.dto.quickSlot.request.PatchQuickSlotRequest;
import chainsawman.gesture.dto.quickSlot.request.QuickSlotRequest;
import chainsawman.gesture.dto.quickSlot.request.UpdateQuickSlotRequest;
import chainsawman.gesture.dto.quickSlot.response.CreateQuickSlotResponse;
import chainsawman.gesture.dto.quickSlot.response.DeleteQuickSlotResponse;
import chainsawman.gesture.dto.quickSlot.response.QuickSlotListResponse;
import chainsawman.gesture.dto.quickSlot.response.QuickSlotPresetResponse;
import chainsawman.gesture.dto.quickSlot.response.UpdateQuickSlotResponse;
import chainsawman.gesture.entity.quick.QuickSlot;
import chainsawman.gesture.entity.quick.QuickSlotPreset;
import chainsawman.gesture.entity.user.User;
import chainsawman.gesture.exceptions.quickslot.QuickSlotLimitExceededException;
import chainsawman.gesture.exceptions.quickslot.QuickSlotNotFoundException;
import chainsawman.gesture.repository.quick.QuickSlotPresetRepository;
import chainsawman.gesture.repository.quick.QuickSlotRepository;
import chainsawman.gesture.security.SecurityUtils;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class QuickSlotService {

    private static final int MAX_LIBRARY_COUNT = 30;
    private static final int MAX_PRESET_COUNT = 5;

    private final QuickSlotRepository quickSlotRepository;
    private final QuickSlotPresetRepository quickSlotPresetRepository;
    private final MediaService mediaService;
    private final SecurityUtils securityUtils;

    @Transactional
    public CreateQuickSlotResponse createQuickSlot(QuickSlotRequest request) {
        User user = securityUtils.getCurrentUser();

        long count = quickSlotRepository.countByUserAndDeletedAtIsNull(user);
        if (count >= MAX_LIBRARY_COUNT) {
            throw new QuickSlotLimitExceededException();
        }

        Set<Integer> usedOrders = quickSlotRepository.findOrdersByUser(user);
        int nextOrder = 0;
        for (int i = 0; i < MAX_LIBRARY_COUNT; i++) {
            if (!usedOrders.contains(i)) {
                nextOrder = i;
                break;
            }
        }

        String iconUrl = mediaService.getMediaUrl(request.getIconUuid()).getFileUrl();

        QuickSlot quickSlot = QuickSlot.builder()
                .user(user)
                .name(request.getName())
                .description(request.getDescription())
                .iconUuid(request.getIconUuid())
                .order(nextOrder)
                .build();

        quickSlotRepository.save(quickSlot);

        return CreateQuickSlotResponse.from(quickSlot, iconUrl);
    }

    @Transactional
    public UpdateQuickSlotResponse updateQuickSlots(UpdateQuickSlotRequest request) {
        User user = securityUtils.getCurrentUser();
        List<Long> ids = request.getQuickSlotIds();

        List<QuickSlot> slots = quickSlotRepository.findByIdxInAndUserAndDeletedAtIsNull(ids, user);
        if (slots.size() != ids.size()) {
            throw new QuickSlotNotFoundException();
        }

        Map<Long, QuickSlot> slotMap = slots.stream()
                .collect(Collectors.toMap(QuickSlot::getIdx, s -> s));
        List<QuickSlot> orderedSlots = ids.stream()
                .map(slotMap::get)
                .collect(Collectors.toList());

        QuickSlotPreset preset = quickSlotPresetRepository.findByUser(user)
                .orElseGet(() -> QuickSlotPreset.create(user));
        preset.updateSlots(orderedSlots);
        QuickSlotPreset saved = quickSlotPresetRepository.save(preset);

        return UpdateQuickSlotResponse.of(user, orderedSlots, saved.getUpdatedAt());
    }

    @Transactional(readOnly = true)
    public List<QuickSlotListResponse> getQuickSlots() {
        User user = securityUtils.getCurrentUser();

        List<QuickSlot> slots = quickSlotRepository.findByUserAndDeletedAtIsNullOrderByOrder(user);

        Set<String> uuids = slots.stream()
                .map(QuickSlot::getIconUuid)
                .collect(Collectors.toSet());
        Map<String, String> urlMap = mediaService.getMediaUrlMap(uuids);

        return slots.stream()
                .map(slot -> QuickSlotListResponse.from(slot, urlMap.getOrDefault(slot.getIconUuid(), null)))
                .collect(Collectors.toList());
    }

    @Transactional(readOnly = true)
    public QuickSlotPresetResponse getPreset() {
        User user = securityUtils.getCurrentUser();

        return quickSlotPresetRepository.findByUser(user)
                .map(preset -> {
                    List<QuickSlot> activeSlots = preset.getActiveSlots();
                    Set<String> uuids = activeSlots.stream()
                            .map(QuickSlot::getIconUuid)
                            .collect(Collectors.toSet());
                    Map<String, String> urlMap = mediaService.getMediaUrlMap(uuids);
                    return QuickSlotPresetResponse.of(user.getIdx(), activeSlots, urlMap, preset.getUpdatedAt());
                })
                .orElseGet(() -> QuickSlotPresetResponse.builder()
                        .userIdx(user.getIdx())
                        .quickSlots(Collections.emptyList())
                        .build());
    }

    @Transactional
    public QuickSlotListResponse patchQuickSlot(Long quickSlotIdx, PatchQuickSlotRequest request) {
        User user = securityUtils.getCurrentUser();

        QuickSlot quickSlot = quickSlotRepository.findByIdxAndUserAndDeletedAtIsNull(quickSlotIdx, user)
                .orElseThrow(QuickSlotNotFoundException::new);

        quickSlot.update(request.getName(), request.getDescription(), request.getIconUuid());

        String iconUrl = mediaService.getMediaUrl(quickSlot.getIconUuid()).getFileUrl();
        return QuickSlotListResponse.from(quickSlot, iconUrl);
    }

    @Transactional
    public DeleteQuickSlotResponse deleteQuickSlot(Long quickSlotIdx) {
        User user = securityUtils.getCurrentUser();
        QuickSlot quickSlot = quickSlotRepository.findByIdxAndUserAndDeletedAtIsNull(quickSlotIdx, user)
                .orElseThrow(QuickSlotNotFoundException::new);

        quickSlot.softDelete();

        quickSlotPresetRepository.findByUser(user).ifPresent(preset -> {
            preset.removeSlot(quickSlotIdx);
        });

        return DeleteQuickSlotResponse.builder()
                .quickSlotIdx(quickSlotIdx)
                .deleted(true)
                .deletedAt(quickSlot.getDeletedAt())
                .build();
    }
}
