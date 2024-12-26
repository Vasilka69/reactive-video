package ru.vasili4.reactive_video.service.impl;

import lombok.RequiredArgsConstructor;
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

import java.util.UUID;

@RequiredArgsConstructor
@Service
public class FileServiceImpl implements FileService {

    private final FileMongoRepository fileMongoRepository;
    private final FileS3Repository fileS3Repository;

    private final UserService userService;
    private final UserHasFileMongoRepository userHasFileMongoRepository;


    @Override
    public Mono<FileDocument> getById(UUID id) {
        return fileMongoRepository.findById(id);
    }

    @Override
    @Transactional
    public Mono<UUID> create(FileRequestEntity dto, byte[] content) {
        return fileMongoRepository.save(new FileDocument(
                        dto.getId(),
                        dto.getBucket(),
                        dto.getFilePath()
                ))
                .doOnSuccess(fileEntity -> fileS3Repository.createFile(new S3File(fileEntity), content))
                .flatMap(fileEntity -> userService.getSecurityUserFromContext()
                        .flatMap(user -> userHasFileMongoRepository.save(
                                new UserHasFileDocument(
                                        new UserHasFileDocument.UserHasFileDocumentId(
                                                user.getUser().getLogin(),
                                                fileEntity.getFileId())
                                )
                        )
                        .doOnError(exception -> System.out.println(exception.getMessage()))
                        .doOnSuccess(entity -> System.out.println("SUCCESS (UserHasFileDocument): " + entity.getId()))
                        .thenReturn(fileEntity))
                )
                .map(FileDocument::getFileId);
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
