package ru.vasili4.reactive_video.security.converters;

import org.springframework.security.core.Authentication;
import org.springframework.security.web.server.authentication.ServerAuthenticationConverter;
import org.springframework.web.server.ServerWebExchange;
import reactor.core.publisher.Mono;
import ru.vasili4.reactive_video.security.TokenAuthenticationService;

public class TokenAuthenticationConverter implements ServerAuthenticationConverter {

    @Override
    public Mono<Authentication> convert(ServerWebExchange exchange) {
        return TokenAuthenticationService.getAuthentication(exchange.getRequest());
    }
}
