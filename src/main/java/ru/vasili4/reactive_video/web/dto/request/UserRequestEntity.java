package ru.vasili4.reactive_video.web.dto.request;

import lombok.Data;
import lombok.EqualsAndHashCode;

@Data
@EqualsAndHashCode
public class UserRequestEntity {
    private String login;
    private String password;
}
