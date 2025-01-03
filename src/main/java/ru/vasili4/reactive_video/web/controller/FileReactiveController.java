package ru.vasili4.reactive_video.web.controller;

import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.http.codec.multipart.FilePart;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import reactor.core.publisher.Mono;
import ru.vasili4.reactive_video.service.FileService;
import ru.vasili4.reactive_video.web.dto.request.FileRequestEntity;
import ru.vasili4.reactive_video.web.dto.response.FileResponseEntity;

import java.nio.file.Paths;
import java.security.Principal;
import java.util.UUID;

@RequiredArgsConstructor
@RestController
@RequestMapping("/api/v1/reactive-file")
public class FileReactiveController {

    private final FileService fileService;

    @GetMapping("/{id}")
    @PreAuthorize("hasPermission('file', #id)")
    public Mono<ResponseEntity<FileResponseEntity>> getById(@PathVariable("id") String id) {
        return fileService.getById(id)
                .map(FileResponseEntity::new)
                .map(ResponseEntity::ok);
    }

    @PostMapping(consumes = {MediaType.MULTIPART_FORM_DATA_VALUE})
    public Mono<ResponseEntity<String>> create(
            Principal principal,
            @RequestPart("bucket") String bucket,
            @RequestPart("filePath") String filePath,
            @RequestPart("file") Mono<FilePart> filePart) {
        return filePart.flatMap(file ->
                        fileService.create(
                                new FileRequestEntity(
                                        UUID.randomUUID().toString(),
                                        bucket,
                                        String.format("%s/%s", Paths.get(filePath).toString().replace("\\", "/"), file.filename())
                                ),
                                principal.getName(),
                                filePart))
                .map(id -> ResponseEntity.status(HttpStatus.CREATED).body(id));
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("hasPermission('file', #id)")
    public Mono<ResponseEntity<Void>> deleteById(
            @PathVariable("id") String id) {
        return fileService.deleteById(id)
                .then(Mono.just(ResponseEntity.status(HttpStatus.ACCEPTED).build()));
    }

}
