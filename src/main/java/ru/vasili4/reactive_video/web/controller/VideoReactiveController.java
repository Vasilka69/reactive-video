package ru.vasili4.reactive_video.web.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.core.io.ByteArrayResource;
import org.springframework.core.io.Resource;
import org.springframework.core.io.buffer.DataBuffer;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpRange;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import ru.vasili4.reactive_video.data.model.s3.S3File;
import ru.vasili4.reactive_video.service.FileService;
import ru.vasili4.reactive_video.utils.ByteArrayUtils;
import ru.vasili4.reactive_video.utils.HttpUtils;
import ru.vasili4.reactive_video.web.dto.response.DataBufferWrapper;

import java.nio.file.Paths;
import java.util.List;
import java.util.concurrent.ExecutionException;

@Tag(name = "api-video-controller", description = "Видео")
@RequiredArgsConstructor
@RestController
@RequestMapping("/api/v1/reactive-video")
public class VideoReactiveController {

    private final FileService fileService;

    @Operation(description = "Синхронное получение видеопотока по ID")
    @GetMapping(value = "/{id}", produces = "video/mp4")
    @PreAuthorize("hasPermission('file', #id)")
    public Mono<ResponseEntity<Resource>> getBlockingVideoStreamById(
            @Parameter(description = "Идентификатор файла", required = true) @PathVariable("id") String id
    ) {
        HttpHeaders headers = new HttpHeaders();
        return fileService.getFileMetadataById(id)
                .doOnSuccess(fileDocument -> headers.setAll(HttpUtils.getFilenameHeaderFromFullPath(fileDocument.getFilePath())))
                .then(fileService.getBlockingFullFileContentById(id)
                        .map(bytes -> new ByteArrayResource(ByteArrayUtils.objectArrayToPrimitiveArray(bytes)))
                        .map(byteArrayResource -> ResponseEntity.ok()
                                .headers(headers)
                                .body(byteArrayResource)));
    }

    @GetMapping(value = "/download/{id}", produces = "video/mp4")
    public ResponseEntity<Flux<DataBuffer>> downloadFile(
            @Parameter(description = "Идентификатор файла", required = true) @PathVariable("id") String id,
            @RequestHeader HttpHeaders headers) throws ExecutionException, InterruptedException {
        S3File s3FileWithoutContentById = fileService.getS3FileWithoutContentById(id).toFuture().get();

        List<HttpRange> ranges = headers.getRange();
//        start = ranges.get(0).getRangeStart(s3FileWithoutContentById.getFileInfo().getSize());
//        end = ranges.get(0).getRangeEnd(s3FileWithoutContentById.getFileInfo().getSize());


        DataBufferWrapper dataBufferFlux = fileService.getNonBlockingFullFileContentById(id).toFuture().get();

        long start = 0;
        long end = 0;

        return ResponseEntity.status(dataBufferFlux.getIsReady() ? 200 : 206)
                .header(HttpHeaders.CONTENT_DISPOSITION, "inline; filename=\"" + Paths.get(s3FileWithoutContentById.getFileDocument().getFilePath()).getFileName() + "\"")
                .header(HttpHeaders.ACCEPT_RANGES, "bytes")
                .header(HttpHeaders.CONTENT_RANGE, "bytes " + ranges.get(0).getRangeStart(s3FileWithoutContentById.getFileInfo().getSize()) + "-" + ranges.get(0).getRangeStart(s3FileWithoutContentById.getFileInfo().getSize()) + dataBufferFlux.getChunkLength() + "/" + dataBufferFlux.getTotalFileSize())
                .contentType(MediaType.valueOf("video/mp4"))
                .contentLength(ranges.get(0).getRangeStart(s3FileWithoutContentById.getFileInfo().getSize()) - ranges.get(0).getRangeStart(s3FileWithoutContentById.getFileInfo().getSize()) + dataBufferFlux.getChunkLength() + 1)
                .body(dataBufferFlux.getDataBuffers());
    }

}
