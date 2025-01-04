package ru.vasili4.reactive_video.security.filters;

import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.http.codec.ServerCodecConfigurer;
import org.springframework.security.authentication.ReactiveAuthenticationManager;
import org.springframework.security.web.server.authentication.AuthenticationWebFilter;
import org.springframework.security.web.server.util.matcher.ServerWebExchangeMatchers;
import reactor.core.publisher.Mono;
import ru.vasili4.reactive_video.security.converters.BodyAuthenticationConverter;

public class JWTLoginFilter extends AuthenticationWebFilter {


    public JWTLoginFilter(HttpMethod method,
                          String url,
                          ReactiveAuthenticationManager authenticationManager,
                          ServerCodecConfigurer serverCodecConfigurer) {
        super(authenticationManager);
        setRequiresAuthenticationMatcher(ServerWebExchangeMatchers.pathMatchers(method, url));
        setAuthenticationSuccessHandler((webFilterExchange, authentication) -> {
            webFilterExchange.getExchange().getResponse().setStatusCode(HttpStatus.OK);
            return Mono.empty();
        });

        setServerAuthenticationConverter(new BodyAuthenticationConverter(serverCodecConfigurer));
    }
}
