package ru.vasili4.reactive_video.client.vk;

public interface VkOauthClient {

    String getAccessToken();
    void refreshToken();
}