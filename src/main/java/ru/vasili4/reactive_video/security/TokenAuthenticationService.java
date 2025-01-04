package ru.vasili4.reactive_video.security;

import io.jsonwebtoken.JwtException;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.SignatureAlgorithm;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpHeaders;
import org.springframework.http.server.reactive.ServerHttpRequest;
import org.springframework.http.server.reactive.ServerHttpResponse;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Mono;

import java.util.Date;

import static org.springframework.util.StringUtils.hasText;

@Slf4j
@Service
public class TokenAuthenticationService {
    private static final String SECRET = "Secret123Secret123Secret123Secret123Secret123Secret123Secret123Secret123Secret123Secret123";
    private static final long EXPIRATION_TIME = 864_000_000;
    private static final String TOKEN_PREFIX = "Bearer ";
    private static final String HEADER_STRING = HttpHeaders.AUTHORIZATION;

    private static SecurityUserDetailsManager securityUserDetailsManager;

    public TokenAuthenticationService(SecurityUserDetailsManager securityUserDetailsManager) {
        TokenAuthenticationService.securityUserDetailsManager = securityUserDetailsManager;
    }

    public static Mono<Authentication> getAuthentication(ServerHttpRequest request) {
        String token = getToken(request);

        if (!hasText(token)) {
            return Mono.empty();
        }

        String userName = getUsername(token);

        return securityUserDetailsManager.findByUsername(userName)
                .cast(SecurityUser.class)
                .map(securityUser -> new UsernamePasswordAuthenticationToken(securityUser, null, securityUser.getAuthorities()));
    }

    public static void addAuthentication(ServerHttpResponse response, String username) {
        response.getHeaders().set(HEADER_STRING, TOKEN_PREFIX + generateToken(username));
    }

    public static String getToken(ServerHttpRequest request) {
        String headerValue = request.getHeaders().getFirst(HEADER_STRING);
        if (headerValue != null)
            return headerValue.replace(TOKEN_PREFIX, "");
        else
            return null;
    }

    private static String generateToken(String username) {
        return Jwts.builder()
                .setSubject(username)
                .setExpiration(new Date(System.currentTimeMillis() + EXPIRATION_TIME))
                .signWith(SignatureAlgorithm.HS512, SECRET)
                .compact();
    }

    public static String getUsername(String token) {
        try {
            return token != null ? Jwts.parser()
                    .setSigningKey(SECRET)
                    .build()
                    .parseClaimsJws(token)
                    .getBody()
                    .getSubject() : null;
        } catch (JwtException e) {
            log.info("Ошибка обработки токена: {}", token);
            return null;
        }
    }
}
