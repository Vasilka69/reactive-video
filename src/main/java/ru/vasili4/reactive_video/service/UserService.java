package ru.vasili4.reactive_video.service;

import jakarta.servlet.http.HttpServletRequest;
import reactor.core.publisher.Mono;
import ru.vasili4.reactive_video.web.dto.request.UserRequestEntity;
import ru.vasili4.reactive_video.web.security.SecurityUser;

public interface UserService {
    Mono<String> register(UserRequestEntity user);
//    SecurityUser getSecurityUserFromRequest(HttpServletRequest request);
    Mono<SecurityUser> getSecurityUserFromContext();
}
