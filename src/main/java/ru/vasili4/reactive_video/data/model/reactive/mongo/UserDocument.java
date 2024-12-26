package ru.vasili4.reactive_video.data.model.reactive.mongo;

import lombok.*;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;
import ru.vasili4.reactive_video.web.dto.request.UserRequestEntity;

@Getter
@Setter
@EqualsAndHashCode
@NoArgsConstructor
@AllArgsConstructor
@Document("user")
public class UserDocument {
    @Id
    private String login;
    private String password;

    public UserDocument(UserRequestEntity dto) {
        this.login = dto.getLogin();
        this.password = dto.getPassword();
    }
}
