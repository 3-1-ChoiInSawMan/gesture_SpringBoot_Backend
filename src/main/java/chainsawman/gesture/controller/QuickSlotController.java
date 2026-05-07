package chainsawman.gesture.controller;

import chainsawman.gesture.dto.quickSlot.request.PatchQuickSlotRequest;
import chainsawman.gesture.dto.quickSlot.request.QuickSlotRequest;
import chainsawman.gesture.dto.quickSlot.request.UpdateQuickSlotRequest;
import chainsawman.gesture.dto.quickSlot.response.CreateQuickSlotResponse;
import chainsawman.gesture.dto.quickSlot.response.DeleteQuickSlotResponse;
import chainsawman.gesture.dto.quickSlot.response.QuickSlotListResponse;
import chainsawman.gesture.dto.quickSlot.response.QuickSlotPresetResponse;
import chainsawman.gesture.dto.quickSlot.response.UpdateQuickSlotResponse;
import chainsawman.gesture.global.ApiResponse;
import chainsawman.gesture.service.QuickSlotService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/quick-slots")
@RequiredArgsConstructor
@Tag(name = "Quick-slots", description = "퀵슬롯 관련 API")
public class QuickSlotController {

    private final QuickSlotService quickSlotService;

    @GetMapping
    @Operation(summary = "내 퀵슬롯 목록 조회", description = "나의 퀵슬롯 라이브러리 전체 목록을 조회합니다.")
    public ResponseEntity<ApiResponse<List<QuickSlotListResponse>>> getQuickSlots() {
        List<QuickSlotListResponse> result = quickSlotService.getQuickSlots();
        return ResponseEntity.ok(ApiResponse.ok(result, "조회되었습니다."));
    }

    @GetMapping("/preset")
    @Operation(summary = "활성 퀵슬롯 프리셋 조회", description = "현재 등록된 활성 퀵슬롯 5개를 조회합니다.")
    public ResponseEntity<ApiResponse<QuickSlotPresetResponse>> getPreset() {
        QuickSlotPresetResponse result = quickSlotService.getPreset();
        return ResponseEntity.ok(ApiResponse.ok(result, "조회되었습니다."));
    }

    @PostMapping
    @Operation(summary = "퀵슬롯 추가", description = "나의 퀵슬롯 라이브러리에 퀵슬롯을 추가합니다. (최대 30개)")
    public ResponseEntity<ApiResponse<CreateQuickSlotResponse>> createQuickSlot(
            @Valid @RequestBody QuickSlotRequest request) {
        CreateQuickSlotResponse result = quickSlotService.createQuickSlot(request);
        return ResponseEntity.ok(ApiResponse.ok(result, "추가되었습니다."));
    }

    @PatchMapping
    @Operation(summary = "프리셋 설정", description = "라이브러리에서 최대 5개를 선택해 활성 퀵슬롯으로 지정합니다.")
    public ResponseEntity<ApiResponse<UpdateQuickSlotResponse>> updateQuickSlots(
            @Valid @RequestBody UpdateQuickSlotRequest request) {
        UpdateQuickSlotResponse result = quickSlotService.updateQuickSlots(request);
        return ResponseEntity.ok(ApiResponse.ok(result, "프리셋이 설정되었습니다."));
    }

    @PatchMapping("/{quickSlotIdx}")
    @Operation(summary = "퀵슬롯 수정", description = "나의 퀵슬롯 정보를 수정합니다. 변경할 필드만 전달하세요.")
    public ResponseEntity<ApiResponse<QuickSlotListResponse>> patchQuickSlot(
            @PathVariable Long quickSlotIdx,
            @RequestBody PatchQuickSlotRequest request) {
        QuickSlotListResponse result = quickSlotService.patchQuickSlot(quickSlotIdx, request);
        return ResponseEntity.ok(ApiResponse.ok(result, "수정되었습니다."));
    }

    @DeleteMapping("/{quickSlotIdx}")
    @Operation(summary = "퀵슬롯 삭제", description = "나의 퀵슬롯 라이브러리에서 퀵슬롯을 삭제합니다.")
    public ResponseEntity<ApiResponse<DeleteQuickSlotResponse>> deleteQuickSlot(
            @PathVariable Long quickSlotIdx) {
        DeleteQuickSlotResponse result = quickSlotService.deleteQuickSlot(quickSlotIdx);
        return ResponseEntity.ok(ApiResponse.ok(result, "삭제되었습니다."));
    }
}
