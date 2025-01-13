package ru.vasili4.reactive_video.exception;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import reactor.core.publisher.Mono;
import ru.vasili4.reactive_video.web.dto.response.ExceptionResponseDto;

@RestControllerAdvice
public class ExceptionController {

    @ExceptionHandler(ResourceIllegalArgumentException.class)
    @ResponseStatus(HttpStatus.BAD_REQUEST)
    public Mono<ExceptionResponseDto> handleIllegalArgumentException(Exception exception) {
        return createErrorResponseDtoMono(exception);
    }

    @ExceptionHandler(EntityValidationException.class)
    @ResponseStatus(HttpStatus.BAD_REQUEST)
    public Mono<ExceptionResponseDto> handleEntityValidationException(Exception exception) {
        return createErrorResponseDtoMono(exception);
    }

    @ExceptionHandler(UserAlreadyExistsException.class)
    @ResponseStatus(HttpStatus.BAD_REQUEST)
    public Mono<ExceptionResponseDto> handleUserAlreadyExistsException(Exception exception) {
        return createErrorResponseDtoMono(exception);
    }

    @ExceptionHandler(UserNotFoundException.class)
    @ResponseStatus(HttpStatus.UNAUTHORIZED)
    public Mono<ExceptionResponseDto> handleUserNotFoundException(Exception exception) {
        return createErrorResponseDtoMono(exception);
    }

    @ExceptionHandler(S3Exception.class)
    @ResponseStatus(HttpStatus.FORBIDDEN)
    public Mono<ExceptionResponseDto> handleS3Exception(Exception exception) {
        return createErrorResponseDtoMono(exception);
    }

    @ExceptionHandler(BaseReactiveVideoException.class)
    @ResponseStatus(HttpStatus.INTERNAL_SERVER_ERROR)
    public Mono<ExceptionResponseDto> handleBaseReactiveVideoException(Exception exception) {
        return createErrorResponseDtoMono(exception);
    }

    private static Mono<ExceptionResponseDto> createErrorResponseDtoMono(Exception e) {
        return Mono.just(createErrorResponseDto(e));
    }

    public static ExceptionResponseDto createErrorResponseDto(Exception e) {
        return new ExceptionResponseDto(e.getMessage());
    }
}
