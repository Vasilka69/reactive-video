package ru.vasili4.reactive_video.security.filters;

import org.springframework.security.web.server.authentication.AuthenticationWebFilter;
import org.springframework.security.web.server.util.matcher.ServerWebExchangeMatchers;
import ru.vasili4.reactive_video.security.converters.TokenAuthenticationConverter;
import ru.vasili4.reactive_video.security.JwtAuthenticationManager;

public class AuthenticationFilter extends AuthenticationWebFilter {

    public AuthenticationFilter(JwtAuthenticationManager jwtAuthenticationManager) {
        super(jwtAuthenticationManager);
        setServerAuthenticationConverter(new TokenAuthenticationConverter());
        setRequiresAuthenticationMatcher(ServerWebExchangeMatchers.pathMatchers("/**"));
    }
}
