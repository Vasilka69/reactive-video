package ru.vasili4.reactive_video.web.dto.response;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@AllArgsConstructor
public class ExceptionResponseEntity {
    private String message;
    private String error;
    private int status;
}

