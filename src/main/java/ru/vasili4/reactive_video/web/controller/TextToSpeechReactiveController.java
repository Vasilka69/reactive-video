package ru.vasili4.reactive_video.web.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.core.io.buffer.DataBuffer;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import reactor.core.publisher.Flux;
import ru.vasili4.reactive_video.service.TextToSpeechService;

@Tag(name = "api-text-to-speech-controller", description = "Текст в речь")
@RequiredArgsConstructor
@RestController
@RequestMapping("/api/v1/reactive/text-to-speech")
public class TextToSpeechReactiveController {

    public static final String AUDIO_MPEG3_CONTENT_TYPE = "audio/mpeg3";

    private final TextToSpeechService textToSpeechService;

    @Operation(description = "Речь из содержимого текстового файла по ID")
    @GetMapping(value = "/{id}", produces = AUDIO_MPEG3_CONTENT_TYPE)
    @PreAuthorize("hasPermission('file', #id)")
    public Flux<DataBuffer> textToSpeechById(
            @Parameter(description = "Идентификатор файла", required = true) @PathVariable("id") String id
    ) {
        return textToSpeechService.textToSpeechById(id);
    }

    @Operation(description = "Речь из содержимого текстового файла по ID с использованием кеша")
    @GetMapping(value = "/cached/{id}", produces = AUDIO_MPEG3_CONTENT_TYPE)
    @PreAuthorize("hasPermission('file', #id)")
    public Flux<DataBuffer> cachedTextToSpeechById(
            @Parameter(description = "Идентификатор файла", required = true) @PathVariable("id") String id
    ) {
        return textToSpeechService.cachedTextToSpeechById(id);
    }
}
