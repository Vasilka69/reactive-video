package ru.vasili4.reactive_video.service;

import reactor.core.publisher.Mono;
import ru.vasili4.reactive_video.web.dto.response.ImageRecognitionResponse;

public interface ImageRecognitionService {
    Mono<ImageRecognitionResponse> recognizeById(String id);

    Mono<ImageRecognitionResponse> cachedRecognizeById(String id);
}
