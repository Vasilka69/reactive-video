package ru.vasili4.reactive_video.web.controller;


import lombok.RequiredArgsConstructor;
import org.springframework.core.io.buffer.DataBufferUtils;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.http.codec.multipart.FilePart;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.context.ReactiveSecurityContextHolder;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.server.ServerWebExchange;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import ru.vasili4.reactive_video.service.FileService;
import ru.vasili4.reactive_video.web.dto.request.FileRequestEntity;
import ru.vasili4.reactive_video.web.dto.response.FileResponseEntity;
import ru.vasili4.reactive_video.web.security.SecurityUser;

import java.io.IOException;
import java.security.Principal;
import java.util.Objects;
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
        return filePart.flatMap(file -> {
            var fileRequestEntity = new FileRequestEntity(
                    UUID.randomUUID().toString(),
                    bucket,
                    String.format("%s/%s", filePath.split("/")[0], file.filename())
            );
            return fileService.create(fileRequestEntity, principal.getName(), filePart);

        }).map(id -> ResponseEntity.status(HttpStatus.CREATED).body(id));

//        filePart.block().content().
//        DataBufferUtils.join()
//        FileUtils.dataBuffersToByteBuffer(filePart.content())
//        return Mono.just(ResponseEntity.status(999).body("))"));
//        return fileService.create(fileRequestEntity, filePart)
//                .map(id -> ResponseEntity.status(HttpStatus.CREATED).body(id));
    }

}
