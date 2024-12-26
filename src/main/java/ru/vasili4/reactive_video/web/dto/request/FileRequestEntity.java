package ru.vasili4.reactive_video.web.dto.request;

import com.fasterxml.jackson.annotation.JsonIgnore;
import lombok.AllArgsConstructor;
import lombok.Data;

import java.util.UUID;

@AllArgsConstructor
@Data
public class FileRequestEntity {
    @JsonIgnore
    private UUID id;
    private String bucket;
    private String filePath;
}
