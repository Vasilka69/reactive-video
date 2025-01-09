package ru.vasili4.reactive_video.security.converters;

import lombok.RequiredArgsConstructor;
import org.springframework.core.ResolvableType;
import org.springframework.http.MediaType;
import org.springframework.http.codec.ServerCodecConfigurer;
import org.springframework.http.server.reactive.ServerHttpRequest;
import org.springframework.http.server.reactive.ServerHttpResponse;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.web.server.authentication.ServerAuthenticationConverter;
import org.springframework.web.server.ServerWebExchange;
import reactor.core.publisher.Mono;
import ru.vasili4.reactive_video.security.TokenAuthenticationService;
import ru.vasili4.reactive_video.web.dto.request.UserRequestDto;

import java.util.Collections;

@RequiredArgsConstructor
public class BodyAuthenticationConverter implements ServerAuthenticationConverter {

    private final ResolvableType usernamePasswordType = ResolvableType.forClass(UserRequestDto.class);

    private final ServerCodecConfigurer serverCodecConfigurer;


    @Override
    public Mono<Authentication> convert(ServerWebExchange exchange) {
        final ServerHttpRequest request = exchange.getRequest();
        final ServerHttpResponse response = exchange.getResponse();

        MediaType contentType = request.getHeaders().getContentType();

        if (contentType != null && contentType.isCompatibleWith(MediaType.APPLICATION_JSON)) {
            return serverCodecConfigurer.getReaders().stream()
                    .filter(reader -> reader.canRead(this.usernamePasswordType, MediaType.APPLICATION_JSON))
                    .findFirst()
                    .orElseThrow(() -> new IllegalStateException("No JSON reader found"))
                    .readMono(this.usernamePasswordType, request, Collections.emptyMap())
                    .cast(UserRequestDto.class)
                    .map(o -> new UsernamePasswordAuthenticationToken(o.getLogin(), o.getPassword()))
                    .doOnSuccess(usernamePasswordAuthenticationTokenSignal ->
                            TokenAuthenticationService.addAuthentication(response, usernamePasswordAuthenticationTokenSignal.getName()))
                    .cast(Authentication.class);
        }
        else {
            return Mono.empty();
        }
    }
}
