package ru.vasili4.reactive_video.web.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import ru.vasili4.reactive_video.service.FileService;
import ru.vasili4.reactive_video.web.dto.response.FileMetadataResponseDto;

import java.security.Principal;

@Tag(name = "api-file-metadata-controller", description = "Метаданные файлов")
@RequiredArgsConstructor
@RestController
@RequestMapping("/api/v1/reactive/file-metadata")
public class FileMetadataReactiveController {

    private final FileService fileService;

    @Operation(description = "Получение списка метаданных файлов пользователя")
    @GetMapping(produces = MediaType.APPLICATION_JSON_VALUE)
    public Flux<FileMetadataResponseDto> getAll(Principal principal) {
        return fileService.getAllMetadataByUserLogin(principal.getName())
                .map(FileMetadataResponseDto::new);
    }

    @Operation(description = "Получение метаданных файла по ID")
    @GetMapping(value = "/{id}", produces = MediaType.APPLICATION_JSON_VALUE)
    @PreAuthorize("hasPermission('file', #id)")
    public Mono<ResponseEntity<FileMetadataResponseDto>> getById(
            @Parameter(description = "Идентификатор файла", required = true) @PathVariable("id") String id) {
        return fileService.getFileMetadataById(id)
                .map(FileMetadataResponseDto::new)
                .map(ResponseEntity::ok);
    }
}
