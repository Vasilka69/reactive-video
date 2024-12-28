package ru.vasili4.reactive_video.web.security.filters;

import org.jetbrains.annotations.NotNull;
import org.springframework.http.server.reactive.ServerHttpRequest;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.ReactiveSecurityContextHolder;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.security.core.context.SecurityContextImpl;
import org.springframework.web.server.ServerWebExchange;
import org.springframework.web.server.WebFilter;
import org.springframework.web.server.WebFilterChain;
import reactor.core.publisher.Mono;
import ru.vasili4.reactive_video.web.security.SecurityUser;
import ru.vasili4.reactive_video.web.security.TokenAuthenticationService;

import java.util.Collections;

public class AuthenticationFilter implements WebFilter {

    @NotNull
    @Override
    public Mono<Void> filter(@NotNull ServerWebExchange exchange, @NotNull WebFilterChain chain) {
        ServerHttpRequest request = exchange.getRequest();

        Mono<Void> filterChain = chain.filter(exchange);

        return filterChain
                .switchIfEmpty(
                        TokenAuthenticationService.getAuthentication(request)
                                .map(ReactiveSecurityContextHolder::withAuthentication)
                                .flatMap(filterChain::contextWrite));
    }

}
