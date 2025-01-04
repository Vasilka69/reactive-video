package ru.vasili4.reactive_video.service;

import reactor.core.publisher.Mono;
import ru.vasili4.reactive_video.web.dto.request.UserRequestEntity;

public interface UserService {
    Mono<String> register(UserRequestEntity user);
//    SecurityUser getSecurityUserFromRequest(HttpServletRequest request);
}
