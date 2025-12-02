package ru.vasili4.reactive_video.web.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import ru.vasili4.reactive_video.service.S3BucketService;

import java.util.List;

@Tag(name = "api-s3-bucket-controller", description = "S3 Бакеты")
@RequiredArgsConstructor
@RestController
@RequestMapping("/api/v1/s3-bucket")
public class S3BucketServiceController {

    private final S3BucketService s3BucketService;

    @Operation(description = "Создание бакета")
    @PostMapping(produces = MediaType.APPLICATION_JSON_VALUE)
    public void createBucket(
            @Parameter(description = "Название бакета", required = true) String bucketName
    ) {
        s3BucketService.createBucket(bucketName);
    }


    @Operation(description = "Получение списка доступных бакетов")
    @GetMapping(produces = MediaType.APPLICATION_JSON_VALUE)
    public List<String> getAllBuckets() {
        return s3BucketService.getAllBuckets();
    }

    @Operation(description = "Удаление бакета")
    @DeleteMapping(produces = MediaType.APPLICATION_JSON_VALUE)
    public void deleteBucket(
            @Parameter(description = "Название бакета", required = true) String bucketName
    ) {
        s3BucketService.deleteBucket(bucketName);
    }

    @Operation(description = "Проверка наличия бакета по его ID")
    @GetMapping(value = "/exists", produces = MediaType.APPLICATION_JSON_VALUE)
    public boolean isBucketExists(
            @Parameter(description = "Название бакета", required = true) String bucketName
    ) {
        return s3BucketService.isBucketExists(bucketName);
    }
}
