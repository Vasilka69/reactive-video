package ru.vasili4.reactive_video.web.dto.response;

import lombok.Data;
import lombok.EqualsAndHashCode;

import java.util.UUID;

@Data
@EqualsAndHashCode
public class FileIdResponseEntity {
    private UUID id;
}