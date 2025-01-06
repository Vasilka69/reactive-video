package ru.vasili4.reactive_video.web.controller;

import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.core.io.ByteArrayResource;
import org.springframework.core.io.Resource;
import org.springframework.core.io.buffer.DataBuffer;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import ru.vasili4.reactive_video.service.FileService;
import ru.vasili4.reactive_video.utils.ByteArrayUtils;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.RandomAccessFile;
import java.util.Arrays;
import java.util.stream.Stream;


@Tag(name = "api-video-controller", description = "Видео")
@RequiredArgsConstructor
@RestController
@RequestMapping("/api/v1/reactive-video")
public class VideoReactiveController {

    private final FileService fileService;

    @GetMapping
    public String test() {
        return "test";
    }

//    @GetMapping(value = "/filik", produces = "video/mp4")
//    @GetMapping(value = "/filik")
    @GetMapping(value = "/filik", produces = MediaType.APPLICATION_OCTET_STREAM_VALUE)
    public ResponseEntity<Flux<Byte>> testFile() throws Exception {
        byte[] bytes = new FileInputStream("src/main/resources/tmp/1.mp4").readAllBytes();
        Byte[] byteObjects = new Byte[bytes.length];
        Arrays.setAll(byteObjects, i -> bytes[i]);

        return ResponseEntity
                .status(HttpStatus.PARTIAL_CONTENT)
                .header(HttpHeaders.CONTENT_RANGE, "bytes " + 0 + "-" + (byteObjects.length - 1) + "/" + (byteObjects.length - 1))
                .header(HttpHeaders.CONTENT_LENGTH, String.valueOf(byteObjects.length - 1))
                .contentType(MediaType.APPLICATION_OCTET_STREAM)
                .body(Flux.fromStream(Stream.of(byteObjects)));
    }

    @GetMapping(value = "/real-reactive-filik")
    public Mono<ResponseEntity<Flux<DataBuffer>>> realReactiveFilik() {

        return null;
    }

//    @GetMapping(value = "/filik", produces = "video/mp4")
//    @GetMapping(value = "/filik")
    @GetMapping(value = "/filikBlocking", produces = MediaType.APPLICATION_OCTET_STREAM_VALUE)
    public ResponseEntity<byte[]> testFileBlocking() throws Exception {
        byte[] bytes = new FileInputStream("src/main/resources/tmp/1.mp4").readAllBytes();
        Byte[] byteObjects = new Byte[bytes.length];
        Arrays.setAll(byteObjects, i -> bytes[i]);

        return ResponseEntity
                .status(HttpStatus.PARTIAL_CONTENT)
                .header(HttpHeaders.CONTENT_RANGE, "bytes " + 0 + "-" + (byteObjects.length - 1) + "/" + (byteObjects.length - 1))
                .header(HttpHeaders.CONTENT_LENGTH, String.valueOf(byteObjects.length - 1))
                .contentType(MediaType.APPLICATION_OCTET_STREAM)
                .body(bytes);
//                .body(byteObjects);
    }

    private static final String FILE_PATH = "src/main/resources/tmp"; // Путь к файлу

//        @GetMapping("/{id}")
//    @PreAuthorize("hasPermission('file', #id)")
//    public Mono<ResponseEntity<FileMetadataResponseDto>> getById(
//            @Parameter(description = "Идентификатор файла", required = true) @PathVariable("id") String id) {

    @GetMapping(value = "/{id}", produces = "video/mp4")
    @PreAuthorize("hasPermission('file', #id)")
    public Mono<Resource> getFileById(
            @Parameter(description = "Идентификатор файла", required = true) @PathVariable("id") String id
    ) {
        return fileService.syncGetFullFileContentById(id)
                .map(ByteArrayUtils::objectArrayToPrimitiveArray)
                .map(ByteArrayResource::new);
    }

//    @GetMapping("/files/{filename}")
    @GetMapping("/files")
    public ResponseEntity<byte[]> getFile(
//            @PathVariable String filename,
                                          @RequestHeader HttpHeaders headers) throws IOException {
        String filename = "1.mp4";
        File file = new File(FILE_PATH + "/" + filename);
        long fileLength = file.length();

        String rangeHeader = headers.getFirst(HttpHeaders.RANGE);
        if (rangeHeader == null) {
            // Если заголовок Range отсутствует, возвращаем весь файл
            return ResponseEntity.ok()
                    .contentType(MediaType.APPLICATION_OCTET_STREAM)
                    .contentLength(fileLength)
                    .body(getFileContent(file, 0, fileLength - 1));
        }

        // Парсим заголовок Range
        String[] ranges = rangeHeader.substring(6).split("-");
        long start = Long.parseLong(ranges[0]);
        long end = (ranges.length > 1) ? Long.parseLong(ranges[1]) : fileLength - 1;

        if (start >= fileLength) {
            // Если запрашиваемый диапазон выходит за пределы файла
            return ResponseEntity.status(HttpStatus.REQUESTED_RANGE_NOT_SATISFIABLE)
                    .header(HttpHeaders.CONTENT_RANGE, "bytes */" + fileLength)
                    .build();
        }

        byte[] content = getFileContent(file, start, end);
        long contentLength = content.length;

        return ResponseEntity.status(HttpStatus.PARTIAL_CONTENT)
                .header(HttpHeaders.CONTENT_RANGE, "bytes " + start + "-" + end + "/" + fileLength)
                .header(HttpHeaders.CONTENT_LENGTH, String.valueOf(contentLength))
                .contentType(MediaType.APPLICATION_OCTET_STREAM)
                .body(content);
    }

    private byte[] getFileContent(File file, long start, long end) throws IOException {
        try (RandomAccessFile raf = new RandomAccessFile(file, "r")) {
            raf.seek(start); // Переходим к началу диапазона
            byte[] bytes = new byte[(int) (end - start + 1)];
            raf.readFully(bytes); // Читаем диапазон
            return bytes;
        }
    }

//    private byte[] testset() throws Exception {
//        DataBufferUtils.
//    }

}
