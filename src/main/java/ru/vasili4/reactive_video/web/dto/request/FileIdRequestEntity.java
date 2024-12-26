package ru.vasili4.reactive_video.web.dto.request;

import lombok.Data;
import lombok.EqualsAndHashCode;

import java.util.UUID;

@Data
@EqualsAndHashCode
public class FileIdRequestEntity {
    private UUID id;
}
