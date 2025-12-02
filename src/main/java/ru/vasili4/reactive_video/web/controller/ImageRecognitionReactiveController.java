package ru.vasili4.reactive_video.web.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import reactor.core.publisher.Mono;
import ru.vasili4.reactive_video.service.ImageRecognitionService;
import ru.vasili4.reactive_video.web.dto.response.ImageRecognitionResponse;

@Tag(name = "api-image-recognition-controller", description = "Распознавание объектов изображений")
@RequiredArgsConstructor
@RestController
@RequestMapping("/api/v1/reactive/image-recognition")
public class ImageRecognitionReactiveController {

    private final ImageRecognitionService imageRecognitionService;

    @Operation(description = "Распознавание объектов изображения по ID")
    @GetMapping(value = "/{id}", produces = MediaType.APPLICATION_JSON_VALUE)
    @PreAuthorize("hasPermission('file', #id)")
    public Mono<ResponseEntity<ImageRecognitionResponse>> recognizeById(
            @Parameter(description = "Идентификатор файла", required = true) @PathVariable("id") String id
    ) {
        return imageRecognitionService.recognizeById(id)
                .map(ResponseEntity::ok);
    }

    @Operation(description = "Распознавание объектов изображения по ID с использованием кеша")
    @GetMapping(value = "/cached/{id}", produces = MediaType.APPLICATION_JSON_VALUE)
    @PreAuthorize("hasPermission('file', #id)")
    public Mono<ResponseEntity<ImageRecognitionResponse>> cachedRecognizeById(
            @Parameter(description = "Идентификатор файла", required = true) @PathVariable("id") String id
    ) {
        return imageRecognitionService.cachedRecognizeById(id)
                .map(ResponseEntity::ok);
    }
}
