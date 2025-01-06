package ru.vasili4.reactive_video.data.model.reactive.mongo;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.ToString;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;
import ru.vasili4.reactive_video.web.dto.request.UserRequestDto;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Document("user")
public class UserDocument {

    @Id
    private String login;
    @ToString.Exclude
    private String password;

    public UserDocument(UserRequestDto dto) {
        this.login = dto.getLogin();
        this.password = dto.getPassword();
    }
}
