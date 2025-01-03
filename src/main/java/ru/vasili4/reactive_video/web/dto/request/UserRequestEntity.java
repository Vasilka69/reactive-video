package ru.vasili4.reactive_video.web.dto.request;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import lombok.EqualsAndHashCode;

@Data
@EqualsAndHashCode
@Schema(description = "Логин и пароль пользователя")
public class UserRequestEntity {
    @Schema(description = "Логин пользователя")
    private String login;
    @Schema(description = "Пароль пользователя")
    private String password;
}
