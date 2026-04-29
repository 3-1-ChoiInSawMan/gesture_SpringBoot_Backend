package chainsawman.gesture.controller;

import chainsawman.gesture.dto.media.response.MediaUploadResponse;
import chainsawman.gesture.global.ApiResponse;
import chainsawman.gesture.service.MediaEntityType;
import chainsawman.gesture.service.MediaService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;

@RestController
@RequestMapping("/medias")
@RequiredArgsConstructor
@Tag(name = "Media", description = "미디어 관련 API")
public class MediaController {

    private final MediaService mediaService;

    @PostMapping(value = "/upload", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    @Operation(summary = "파일 업로드", description = "s3에 파일을 업로드 하는 API 입니다.")
    public ResponseEntity<ApiResponse<MediaUploadResponse>>
        upload(@Parameter(description = "업로드할 파일") @RequestPart("file") MultipartFile file,
           @RequestParam("entityType") MediaEntityType entityType) throws IOException {
        MediaUploadResponse result = mediaService.upload(file, entityType);
        return ResponseEntity.ok(ApiResponse.ok(result, "파일이 업로드되었습니다."));
    }


}
