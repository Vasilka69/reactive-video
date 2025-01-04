package ru.vasili4.reactive_video.security.filters;

import lombok.RequiredArgsConstructor;
import org.jetbrains.annotations.NotNull;
import org.springframework.http.server.reactive.ServerHttpRequest;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.ReactiveSecurityContextHolder;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.context.SecurityContextImpl;
import org.springframework.security.web.server.context.ServerSecurityContextRepository;
import org.springframework.web.server.ServerWebExchange;
import org.springframework.web.server.WebFilter;
import org.springframework.web.server.WebFilterChain;
import reactor.core.Disposable;
import reactor.core.publisher.Mono;
import ru.vasili4.reactive_video.security.TokenAuthenticationService;

@RequiredArgsConstructor
public class AuthenticationFilter implements WebFilter {

    private final ServerSecurityContextRepository repository;

    @NotNull
    @Override
    public Mono<Void> filter(@NotNull ServerWebExchange exchange, @NotNull WebFilterChain chain) {
        ServerHttpRequest request = exchange.getRequest();

//        Disposable tokenAuthentication = TokenAuthenticationService.getAuthentication(request)
////                .doOnSuccess(authentication -> repository.save(exchange, new SecurityContextImpl(authentication)).subscribe())
////                .doOnSuccess(authentication -> SecurityContextHolder.getContext().setAuthentication(authentication))
////                .map(ReactiveSecurityContextHolder::withAuthentication)
////                                .flatMap(chain::contextWrite)
//                .subscribe();

        Mono<Void> filterChain = chain.filter(exchange);

//        return filterChain
////                .switchIfEmpty(
////                        tokenAuthentication
////                                .map(ReactiveSecurityContextHolder::withAuthentication)
////                                .flatMap(filterChain::contextWrite);
        return filterChain
                .switchIfEmpty(
                        TokenAuthenticationService.getAuthentication(request)
                                .map(ReactiveSecurityContextHolder::withAuthentication)
                                .flatMap(filterChain::contextWrite));
    }

}
