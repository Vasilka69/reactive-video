package ru.vasili4.reactive_video.service.impl;

import lombok.RequiredArgsConstructor;
import org.springframework.core.io.buffer.DataBuffer;
import org.springframework.core.io.buffer.DataBufferUtils;
import org.springframework.http.codec.multipart.FilePart;
import org.springframework.http.codec.multipart.Part;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import reactor.core.publisher.Mono;
import ru.vasili4.reactive_video.data.model.reactive.mongo.FileDocument;
import ru.vasili4.reactive_video.data.model.reactive.mongo.UserHasFileDocument;
import ru.vasili4.reactive_video.data.model.s3.S3File;
import ru.vasili4.reactive_video.data.repository.reactive.mongo.FileMongoRepository;
import ru.vasili4.reactive_video.data.repository.reactive.mongo.UserHasFileMongoRepository;
import ru.vasili4.reactive_video.data.repository.s3.FileS3Repository;
import ru.vasili4.reactive_video.service.FileService;
import ru.vasili4.reactive_video.service.UserService;
import ru.vasili4.reactive_video.web.dto.request.FileRequestEntity;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.UUID;

@RequiredArgsConstructor
@Service
public class FileServiceImpl implements FileService {

    private final FileMongoRepository fileMongoRepository;
    private final FileS3Repository fileS3Repository;

    private final UserService userService;
    private final UserHasFileMongoRepository userHasFileMongoRepository;


    @Override
    public Mono<FileDocument> getById(String id) {
        return fileMongoRepository.findById(id);
    }

    @Override
    @Transactional
    public Mono<String> create(FileRequestEntity dto, String login, Mono<FilePart> filePartMono) {
        return fileMongoRepository.save(new FileDocument(
                        dto.getId(),
                        dto.getBucket(),
                        dto.getFilePath()
                ))
                .flatMap(fileEntity ->
                        filePartMono.flatMap(filePart ->
                                DataBufferUtils.join(filePart.content())
                                        .map(dataBuffer -> {
                                            byte[] byteContent = new byte[dataBuffer.readableByteCount()];
                                            dataBuffer.read(byteContent);
                                            DataBufferUtils.release(dataBuffer);
                                            fileS3Repository.createFile(new S3File(fileEntity), byteContent);
                                            return fileEntity;
                                        })
                        )
                )
                .flatMap(fileEntity -> userHasFileMongoRepository.save(
                                new UserHasFileDocument(
                                        new UserHasFileDocument.UserHasFileDocumentId(
                                                login,
                                                fileEntity.getFileId())
                                ))
                        .thenReturn(fileEntity)
                )
                .map(FileDocument::getFileId);
//    @Override
//    @Transactional
//    public Mono<String> create(FileRequestEntity dto, String login, Mono<FilePart> filePart) {
//        return fileMongoRepository.save(new FileDocument(
//                        dto.getId(),
//                        dto.getBucket(),
//                        dto.getFilePath()
//                ))
//                .map(fileEntity -> {
//                    List<Byte> byteList = new ArrayList<>();
//
//                    filePart
//                            .map(dataBufferFlux -> dataBufferFlux
//                                    .content()
//                                    .doOnEach(dataBufferSignal -> {
//                                        DataBuffer dataBuffer = dataBufferSignal.get();
//                                        if (dataBuffer == null)
//                                            return;
//                                        byte[] buffer = new byte[dataBuffer.readableByteCount()];
//                                        dataBuffer.read(buffer);
//                                        for (byte b : buffer) {
//                                            byteList.add(b);
//                                        }
//                                    }))
//                            .subscribe();
//
//                    byte[] byteContent = new byte[byteList.size()];
//                    for (int i = 0; i < byteList.size(); i++) {
//                        byteContent[i] = byteList.get(i);
//                    }
//                    fileS3Repository.createFile(new S3File(fileEntity), byteContent);
//                    return fileEntity;
//                })
//                .flatMap(fileEntity -> userHasFileMongoRepository.save(
//                                new UserHasFileDocument(
//                                        new UserHasFileDocument.UserHasFileDocumentId(
//                                                login,
//                                                fileEntity.getFileId())
//                                ))
//                        .thenReturn(fileEntity)
//                )
//                .map(FileDocument::getFileId);
//        return fileMongoRepository.save(new FileDocument(
//                        dto.getId(),
//                        dto.getBucket(),
//                        dto.getFilePath()
//                ))
//                .doOnSuccess(fileEntity -> fileS3Repository.createFile(new S3File(fileEntity), content))
//                .flatMap(fileEntity -> userHasFileMongoRepository.save(
//                                new UserHasFileDocument(
//                                        new UserHasFileDocument.UserHasFileDocumentId(
//                                                userService.getSecurityUserFromRequest(null).getLogin(),
//                                                fileEntity.getFileId())
//                                )
//                        )
//                        .doOnError(exception -> System.out.println(exception.getMessage()))
//                        .doOnSuccess(entity -> System.out.println("SUCCESS (UserHasFileDocument): " + entity.getId()))
//                        .thenReturn(fileEntity)
//                )
//                .map(FileDocument::getFileId);
    }
}
