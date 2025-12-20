package ru.vasili4.reactive_video.client.vk;

import org.springframework.core.io.buffer.DataBuffer;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import ru.vasili4.reactive_video.client.vk.dto.DetectResponse;

public interface VkVisionClient {
    Mono<DetectResponse> detect(Flux<DataBuffer> filePublisher);
}
