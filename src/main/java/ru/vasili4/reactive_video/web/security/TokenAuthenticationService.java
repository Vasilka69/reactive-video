package ru.vasili4.reactive_video.web.security;

import io.jsonwebtoken.JwtException;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.SignatureAlgorithm;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Service;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.util.Date;

import static org.springframework.util.StringUtils.hasText;


@Service
public class TokenAuthenticationService {
    private static final String SECRET = "Secret123Secret123Secret123Secret123Secret123Secret123Secret123Secret123Secret123Secret123";
    private static final long EXPIRATION_TIME = 864_000_000;
    private static final String TOKEN_PREFIX = "Bearer ";
    private static final String HEADER_STRING = "Authorization";

    private static Logger LOG = LoggerFactory.getLogger(TokenAuthenticationService.class);

    private static SecurityUserDetailsManager securityUserDetailsManager;

    public TokenAuthenticationService(SecurityUserDetailsManager securityUserDetailsManager) {
        this.securityUserDetailsManager = securityUserDetailsManager;
    }

    public static void addAuthentication(HttpServletResponse response, String username) {
        response.addHeader(HEADER_STRING, TOKEN_PREFIX + generateToken(username));
    }

    public static Authentication getAuthentication(HttpServletRequest request) {
        String token = getToken(request);

        if (!hasText(token)) {
            return null;
        }

        String userName = getUsername(token);

        SecurityUser user = (SecurityUser) securityUserDetailsManager.loadUserByUsername(userName);
        return new UsernamePasswordAuthenticationToken(user, null, user.getAuthorities());
    }

    private static String generateToken(String username) {
        return Jwts.builder()
                .setSubject(username)
                .setExpiration(new Date(System.currentTimeMillis() + EXPIRATION_TIME))
                .signWith(SignatureAlgorithm.HS512, SECRET)
                .compact();
    }

    private static String getToken(HttpServletRequest request) {
        if (request.getHeader(HEADER_STRING) != null)
            return request.getHeader(HEADER_STRING).replace(TOKEN_PREFIX, "");
        else
            return null;
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
            LOG.info("Ошибка обработки токена: {}", token);
            return null;
        }
    }
}
