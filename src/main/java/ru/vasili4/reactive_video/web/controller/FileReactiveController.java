package ru.vasili4.reactive_video.web.controller;


import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import reactor.core.publisher.Mono;
import ru.vasili4.reactive_video.service.FileService;
import ru.vasili4.reactive_video.web.dto.request.FileRequestEntity;
import ru.vasili4.reactive_video.web.dto.response.FileResponseEntity;

import java.io.IOException;
import java.util.UUID;

@RequiredArgsConstructor
@RestController
@RequestMapping("/api/v1/reactive-file")
public class FileReactiveController {

    private final FileService fileService;

    @GetMapping("/{id}")
    public Mono<ResponseEntity<FileResponseEntity>> getById(@PathVariable("id") UUID id) {
        return fileService.getById(id)
                .map(FileResponseEntity::new)
                .map(ResponseEntity::ok);
    }

    @PostMapping(consumes = {"multipart/form-data"})
    public Mono<ResponseEntity<UUID>> create(
            @RequestPart("bucket") String bucket,
            @RequestPart("filePath") String filePath,
            @RequestPart("file") MultipartFile file) {
        var fileRequestEntity = new FileRequestEntity(UUID.randomUUID(), bucket, filePath);
//        var fileRequestEntity = new FileRequestEntity(null, bucket, filePath);
        try {
            return fileService.create(fileRequestEntity, file.getBytes())
                    .map(id -> ResponseEntity.status(HttpStatus.CREATED).body(id));
        } catch (IOException e) {
            throw new IllegalArgumentException(e);
        }
    }

}
