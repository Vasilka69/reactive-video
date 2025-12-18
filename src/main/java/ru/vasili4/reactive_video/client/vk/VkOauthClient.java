package ru.vasili4.reactive_video.client.vk;

import reactor.core.publisher.Mono;

public interface VkOauthClient {

    String getAccessToken(String oauthClientId);
    String getAccessToken(String oauthClientId, String oauthRefreshToken);
    Mono<String> refreshToken(String oauthClientId, String oauthRefreshToken);
}