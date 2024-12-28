package ru.vasili4.reactive_video.web.security;

import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpHeaders;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.ReactiveAuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.security.core.context.SecurityContextImpl;
import org.springframework.security.web.server.context.ServerSecurityContextRepository;
import org.springframework.web.server.ServerWebExchange;
import reactor.core.publisher.Mono;

import java.util.HashMap;
import java.util.Map;

//@RequiredArgsConstructor
public class DefaultSecurityContextRepository implements ServerSecurityContextRepository {

//    private final ReactiveAuthenticationManager authenticationManager;
    private final Map<ServerWebExchange, SecurityContext> storage = new HashMap<>();

    @Override
    public Mono<Void> save(ServerWebExchange exchange, SecurityContext context) {
        storage.put(exchange, context);
        return Mono.empty();
    }

    @Override
    public Mono<SecurityContext> load(ServerWebExchange exchange) {
        return Mono.justOrEmpty(storage.get(exchange));
    }

//    @Override
//    public Mono<SecurityContext> load(ServerWebExchange swe) {
//        Mono<String> stringMono = Mono.justOrEmpty(swe.getRequest().getHeaders().getFirst(HttpHeaders.AUTHORIZATION));
//        return stringMono.flatMap(this::getSecurityContext);
//    }
//
//    private Mono<? extends SecurityContext> getSecurityContext(String token) {
//        Authentication auth = new UsernamePasswordAuthenticationToken(token, token);
//        return authenticationManager.authenticate(auth).map(SecurityContextImpl::new);
//    }
}
