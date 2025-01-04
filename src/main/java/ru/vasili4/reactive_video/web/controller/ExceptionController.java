package ru.vasili4.reactive_video.web.controller;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.ResponseStatus;
import reactor.core.publisher.Mono;
import ru.vasili4.reactive_video.exception.S3Exception;
import ru.vasili4.reactive_video.web.dto.response.ExceptionResponseEntity;

@ControllerAdvice
public class ExceptionController {

    @ExceptionHandler(S3Exception.class)
    @ResponseStatus(HttpStatus.FORBIDDEN)
    public Mono<ExceptionResponseEntity> handleS3Exception(S3Exception exception) {
        ExceptionResponseEntity errorResponseEntity = createErrorResponseEntity(exception, HttpStatus.BAD_REQUEST);

        return Mono.just(errorResponseEntity);
    }

    private static ExceptionResponseEntity createErrorResponseEntity(Exception e, HttpStatus httpStatus) {
        return new ExceptionResponseEntity(e.getMessage(), httpStatus.getReasonPhrase(), httpStatus.value());
    }
}
