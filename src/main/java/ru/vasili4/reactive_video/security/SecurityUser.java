package ru.vasili4.reactive_video.security;

import lombok.Getter;
import lombok.Setter;
import org.springframework.security.core.userdetails.User;
import ru.vasili4.reactive_video.data.model.reactive.mongo.UserDocument;

import java.util.Collection;

@Getter
@Setter
public class SecurityUser extends User {

    private UserDocument user;
    public SecurityUser(UserDocument user, String username, String password, Collection<? extends SecurityPermission> authorities) {
        super(username, password, authorities);
        this.user = user;
    }
}
