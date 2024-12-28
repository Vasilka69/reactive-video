package ru.vasili4.reactive_video.web.security.dto;

import lombok.Getter;

@Getter
public class AccountCredentials {
    private String login;
    private String password;
}
