package ru.vasili4.reactive_video.web.dto.response;

import lombok.AllArgsConstructor;
import lombok.Data;
import org.springframework.core.io.buffer.DataBuffer;
import reactor.core.publisher.Flux;

@Data
@AllArgsConstructor
public class DataBufferWrapper {
    private Flux<DataBuffer> dataBuffers;
    private Long chunkLength;
    private Long totalFileSize;
    private Boolean isReady;
}
