package ru.vasili4.reactive_video.web.security;

import org.springframework.security.access.PermissionEvaluator;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.userdetails.User;
import org.springframework.stereotype.Component;
import ru.vasili4.reactive_video.web.exception.UserNotFoundException;

import java.io.Serializable;

@Component
public class DefaultPermissionEvaluator implements PermissionEvaluator {

    @Override
    public boolean hasPermission(Authentication authentication, Object targetDomainObject, Object fileId) {
        SecurityUser user;
        if (authentication.getPrincipal() instanceof User)
            user = (SecurityUser) authentication.getPrincipal();
        else
            throw new UserNotFoundException("Ошибка получения пользователя");

        return user.getAuthorities().stream().map(GrantedAuthority::getAuthority).toList().contains(fileId.toString());
    }

    @Override
    public boolean hasPermission(Authentication authentication, Serializable serializable, String s, Object o) {
        return false;
    }
}
