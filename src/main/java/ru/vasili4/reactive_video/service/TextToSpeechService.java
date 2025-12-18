package ru.vasili4.reactive_video.service;

import org.springframework.core.io.buffer.DataBuffer;
import reactor.core.publisher.Flux;

public interface TextToSpeechService {
    Flux<DataBuffer> textToSpeechById(String id);
    Flux<DataBuffer> cachedTextToSpeechById(String id);
}
