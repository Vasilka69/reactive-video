package ru.vasili4.reactive_video.integration.controller;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.reactive.AutoConfigureWebTestClient;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.annotation.Import;
import org.springframework.core.io.buffer.DataBuffer;
import org.springframework.core.io.buffer.DefaultDataBufferFactory;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.client.MultipartBodyBuilder;
import org.springframework.test.web.reactive.server.WebTestClient;
import org.springframework.web.reactive.function.BodyInserters;
import reactor.core.publisher.Flux;
import ru.vasili4.reactive_video.config.TestConfig;
import ru.vasili4.reactive_video.data.model.s3.S3File;
import ru.vasili4.reactive_video.data.model.s3.S3FileInfo;
import ru.vasili4.reactive_video.data.model.s3.S3FileLocation;
import ru.vasili4.reactive_video.web.dto.request.UserRequestDto;

import java.util.Map;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertArrayEquals;

@Import(TestConfig.class)
@SpringBootTest
@AutoConfigureWebTestClient
@DisplayName("Интеграционные тесты контроллера файлов")
class FileReactiveControllerIntegrationTest {

    @Autowired
    private WebTestClient webTestClient;

    private String authorizationToken = "";

    private S3File s3File = null;
    private String createdFileId = "";

    @BeforeEach
    void setUp() {
        UserRequestDto user = new UserRequestDto(UUID.randomUUID().toString(), "Password");
        this.s3File = new S3File(
                new S3FileLocation("test-bucket", "1"),
                new S3FileInfo((long) "Content_1".getBytes().length),
                "Content_1".getBytes());

        webTestClient.post()
                .uri("/api/v1/reactive/user/register")
                .contentType(MediaType.APPLICATION_JSON)
                .bodyValue(user)
                .exchange()
                .expectStatus().isOk();

        this.authorizationToken = webTestClient.post()
                .uri("/api/v1/reactive/user/login")
                .contentType(MediaType.APPLICATION_JSON)
                .bodyValue(user)
                .exchange()
                .expectStatus().isOk()
                .returnResult(Void.class)
                .getResponseHeaders()
                .getFirst(HttpHeaders.AUTHORIZATION);

        MultipartBodyBuilder bodyBuilder = new MultipartBodyBuilder();
        bodyBuilder.part("bucket", this.s3File.getS3FileLocation().getBucket(), MediaType.TEXT_PLAIN);
        bodyBuilder.part("filePath", this.s3File.getS3FileLocation().getFilePath(), MediaType.TEXT_PLAIN);
        bodyBuilder.asyncPart("file",
                Flux.just(DefaultDataBufferFactory.sharedInstance.wrap(this.s3File.getContent())), DataBuffer.class)
                .filename(String.format("%s.txt", UUID.randomUUID()));

        this.createdFileId = webTestClient.post()
                .uri("/api/v1/reactive/file")
                .contentType(MediaType.MULTIPART_FORM_DATA)
                .header(HttpHeaders.AUTHORIZATION, authorizationToken)
                .body(BodyInserters.fromMultipartData(bodyBuilder.build()))
                .exchange()
                .expectHeader().contentTypeCompatibleWith(MediaType.APPLICATION_JSON)
                .expectStatus().isCreated()
                .returnResult(String.class)
                .getResponseBody()
                .blockLast();

    }

    @AfterEach
    void tearDown() {
        // when
        webTestClient.delete()
                .uri(uriBuilder -> uriBuilder.path("/api/v1/reactive/file/{id}").build(Map.of("id", createdFileId)))
                .header(HttpHeaders.AUTHORIZATION, authorizationToken)
                .header(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON_VALUE)
                .exchange()
                // then
                .expectStatus().isAccepted();
    }

    @Test
    @DisplayName("Успешное асинхронное получение потока содержимого файла по ID")
    void asyncGetFileStreamById_FileIsExists_ReturnsAsyncFileContent() {
        // when
        byte[] actualContent = webTestClient.get()
                .uri(uriBuilder -> uriBuilder.path("/api/v1/reactive/file/async/{id}").build(Map.of("id", createdFileId)))
                .header(HttpHeaders.AUTHORIZATION, authorizationToken)
                .header(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON_VALUE)
                .exchange()
                // then
                .expectHeader().contentTypeCompatibleWith(MediaType.APPLICATION_OCTET_STREAM)
                .expectStatus().isOk()
                .expectBody()
                .returnResult().getResponseBody();

        assertArrayEquals(s3File.getContent(), actualContent);
    }

    @Test
    @DisplayName("Попытка асинхронного получения потока содержимого файла, к которому у пользователя нет доступа")
    void asyncGetFileStreamById_FileDoesNotExists_ReturnsAccessDenied() {
        // when
        webTestClient.get()
                .uri(uriBuilder -> uriBuilder.path("/api/v1/reactive/file/async/{id}")
                        .build(Map.of("id", "ab37852f-f9b9-40da-9500-95ffb65230c6")))
                .header(HttpHeaders.AUTHORIZATION, authorizationToken)
                .header(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON_VALUE)
                .exchange()
                // then
                .expectStatus().isForbidden()
                .expectBody();
    }
}