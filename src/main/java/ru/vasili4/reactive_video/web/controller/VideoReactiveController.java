package ru.vasili4.reactive_video.web.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.core.io.ByteArrayResource;
import org.springframework.core.io.Resource;
import org.springframework.core.io.buffer.DataBuffer;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import ru.vasili4.reactive_video.service.FileService;
import ru.vasili4.reactive_video.utils.ByteArrayUtils;

@Tag(name = "api-video-controller", description = "Видео")
@RequiredArgsConstructor
@RestController
@RequestMapping("/api/v1/reactive/video")
public class VideoReactiveController {

    private final FileService fileService;

    @Operation(description = "Синхронное получение видеопотока по ID")
    @GetMapping(value = "/sync/{id}", produces = "video/mp4")
    @PreAuthorize("hasPermission('file', #id)")
    public Mono<Resource> syncGetVideoStreamById(
            @Parameter(description = "Идентификатор файла", required = true) @PathVariable("id") String id
    ) {
        return fileService.getFileMetadataById(id)
                .then(fileService.syncGetFullFileContentById(id)
                        .map(bytes -> new ByteArrayResource(ByteArrayUtils.objectArrayToPrimitiveArray(bytes))));
    }

    @Operation(description = "Асинхронное получение видеопотока по ID")
    @GetMapping(value = "/async/{id}", produces = "video/mp4")
    @PreAuthorize("hasPermission('file', #id)")
    public Flux<DataBuffer> asyncGetVideoStreamById(
            @Parameter(description = "Идентификатор файла", required = true) @PathVariable("id") String id) {
        return fileService.asyncGetFullFileContentById(id);
    }
}
