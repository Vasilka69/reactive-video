package ru.vasili4.reactive_video.client.vk;

import org.springframework.core.io.buffer.DataBuffer;
import reactor.core.publisher.Flux;

public interface VkVoiceClient {
    Flux<DataBuffer> textToSpeech(String textToSpeech);
}
