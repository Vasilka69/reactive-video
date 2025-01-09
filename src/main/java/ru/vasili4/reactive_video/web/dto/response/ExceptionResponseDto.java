package ru.vasili4.reactive_video.web.dto.response;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@AllArgsConstructor
@Schema(description = "DTO сообщения об ошибке")
public class ExceptionResponseDto {
    @Schema(description = "Сообщение об ошибке")
    private String message;
}

