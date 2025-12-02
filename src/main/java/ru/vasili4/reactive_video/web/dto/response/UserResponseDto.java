package ru.vasili4.reactive_video.web.dto.response;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import ru.vasili4.reactive_video.data.model.reactive.mongo.UserDocument;

@Data
@Schema(description = "Информация о польхователе")
public class UserResponseDto {

    @Schema(description = "Логин")
    private String login;

    public UserResponseDto(UserDocument user) {
        this.login = user.getLogin();
    }
}
