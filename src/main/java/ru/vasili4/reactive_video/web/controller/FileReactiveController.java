package ru.vasili4.reactive_video.web.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.core.io.ByteArrayResource;
import org.springframework.core.io.Resource;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.http.codec.multipart.FilePart;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import ru.vasili4.reactive_video.data.model.reactive.mongo.FileDocument;
import ru.vasili4.reactive_video.service.FileService;
import ru.vasili4.reactive_video.utils.ByteArrayUtils;
import ru.vasili4.reactive_video.utils.HttpUtils;
import ru.vasili4.reactive_video.web.dto.response.FileMetadataResponseDto;

import java.nio.file.Paths;
import java.security.Principal;
import java.util.UUID;

@Tag(name = "api-file-controller", description = "Файлы")
@RequiredArgsConstructor
@RestController
@RequestMapping("/api/v1/reactive-file")
public class FileReactiveController {

    private final FileService fileService;

    @Operation(description = "Получение списка метаданных файлов пользователя")
    @GetMapping
    public Flux<FileMetadataResponseDto> getAll(Principal principal) {
        return fileService.getAllMetadataByUserLogin(principal.getName())
                .map(FileMetadataResponseDto::new);
    }

    @Operation(description = "Получение метаданных файла по ID")
    @GetMapping("/{id}")
    @PreAuthorize("hasPermission('file', #id)")
    public Mono<ResponseEntity<FileMetadataResponseDto>> getById(
            @Parameter(description = "Идентификатор файла", required = true) @PathVariable("id") String id) {
        return fileService.getFileMetadataById(id)
                .map(FileMetadataResponseDto::new)
                .map(ResponseEntity::ok);
    }

    @Operation(description = "Синхронное получение потока содержимого файла по ID")
    @GetMapping(value = "/blocking/{id}", produces = MediaType.APPLICATION_OCTET_STREAM_VALUE)
    @PreAuthorize("hasPermission('file', #id)")
    public Mono<ResponseEntity<Resource>> getBlockingFileById(
            @Parameter(description = "Идентификатор файла", required = true) @PathVariable("id") String id
    ) {
        HttpHeaders headers = new HttpHeaders();
        return fileService.getFileMetadataById(id)
                .doOnSuccess(fileDocument -> headers.setAll(HttpUtils.getContentDispositionHeaderByPath(fileDocument.getFilePath())))
                .then(fileService.blockingGetFullFileContentById(id)
                        .map(bytes -> new ByteArrayResource(ByteArrayUtils.objectArrayToPrimitiveArray(bytes)))
                        .map(byteArrayResource -> ResponseEntity.ok()
                                .headers(headers)
                                .body(byteArrayResource)));
    }

    @Operation(description = "Загрузка файла в хранилище")
    @PostMapping(consumes = {MediaType.MULTIPART_FORM_DATA_VALUE})
    public Mono<ResponseEntity<String>> create(
            Principal principal,
            @Parameter(description = "Bucket S3 хранилища для файла", required = true) @RequestPart("bucket") String bucket,
            @Parameter(description = "Путь файла в S3 хранилище", required = true) @RequestPart("filePath") String filePath,
            @Parameter(description = "Файл", required = true) @RequestPart("file") Mono<FilePart> filePart) {
        return filePart.flatMap(file ->
                        fileService.create(
                                new FileDocument(
                                        UUID.randomUUID().toString(),
                                        bucket,
                                        String.format("%s/%s", Paths.get(filePath).toString().replace("\\", "/"), file.filename())
                                ),
                                filePart,
                                principal.getName()))
                .map(id -> ResponseEntity.status(HttpStatus.CREATED).body(id));
    }

    @Operation(description = "Обновление содержимого файла")
    @PostMapping(value = "/{id}", consumes = {MediaType.MULTIPART_FORM_DATA_VALUE})
    @PreAuthorize("hasPermission('file', #id)")
    public Mono<ResponseEntity<String>> update(
            @Parameter(description = "Идентификатор файла", required = true) @PathVariable("id") String id,
            @Parameter(description = "Файл", required = true) @RequestPart("file") Mono<FilePart> filePart) {
        return filePart.flatMap(file ->
                        fileService.updateFileContent(
                                id,
                                filePart))
                .map(fileId -> ResponseEntity.status(HttpStatus.CREATED).body(fileId));
    }

    @Operation(description = "Удаление файла по ID")
    @DeleteMapping("/{id}")
    @PreAuthorize("hasPermission('file', #id)")
    public Mono<ResponseEntity<Void>> deleteById(
            @Parameter(description = "Идентификатор файла", required = true) @PathVariable("id") String id) {
        return fileService.deleteById(id)
                .then(Mono.just(ResponseEntity.status(HttpStatus.ACCEPTED).build()));
    }
}
