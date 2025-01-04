package ru.vasili4.reactive_video.service.impl;

import lombok.RequiredArgsConstructor;
import org.springframework.core.io.buffer.DataBufferUtils;
import org.springframework.http.codec.multipart.FilePart;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import ru.vasili4.reactive_video.data.model.reactive.mongo.FileDocument;
import ru.vasili4.reactive_video.data.model.reactive.mongo.UserHasFileDocument;
import ru.vasili4.reactive_video.data.model.s3.S3File;
import ru.vasili4.reactive_video.data.repository.reactive.FileReactiveRepository;
import ru.vasili4.reactive_video.data.repository.reactive.UserHasFileReactiveRepository;
import ru.vasili4.reactive_video.data.repository.s3.S3FileRepository;
import ru.vasili4.reactive_video.service.FileService;

@RequiredArgsConstructor
@Service
public class FileServiceImpl implements FileService {

    private final FileReactiveRepository fileReactiveRepository;
    private final S3FileRepository s3FileRepository;

    private final UserHasFileReactiveRepository userHasFileReactiveRepository;


    @Override
    public Mono<FileDocument> getById(String id) {
        return fileReactiveRepository.findById(id);
    }

    @Override
    @Transactional
    public Mono<String> create(FileDocument fileDocument, String login, Mono<FilePart> filePartMono) {
        return fileReactiveRepository.save(new FileDocument(
                        fileDocument.getFileId(),
                        fileDocument.getBucket(),
                        fileDocument.getFilePath()
                ))
                .flatMap(fileEntity ->
                        filePartMono.flatMap(filePart ->
                                DataBufferUtils.join(filePart.content())
                                        .map(dataBuffer -> {
                                            byte[] byteContent = new byte[dataBuffer.readableByteCount()];
                                            dataBuffer.read(byteContent);
                                            DataBufferUtils.release(dataBuffer);
                                            s3FileRepository.createFile(new S3File(fileEntity), byteContent);
                                            return fileEntity;
                                        })
                        )
                )
                .flatMap(fileEntity -> userHasFileReactiveRepository.save(
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
//    public Mono<String> create(FileRequestEntity fileDocument, String login, Mono<FilePart> filePart) {
//        return fileMongoRepository.save(new FileDocument(
//                        fileDocument.getId(),
//                        fileDocument.getBucket(),
//                        fileDocument.getFilePath()
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
//                        fileDocument.getId(),
//                        fileDocument.getBucket(),
//                        fileDocument.getFilePath()
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

    @Override
    public Mono<Void> deleteById(String id) {
        return fileReactiveRepository.findById(id)
                .doOnSuccess(fileReactiveRepository::delete)
                .doOnSuccess(fileDocument -> s3FileRepository.deleteFile(new S3File(fileDocument)))
                .doOnSuccess(fileDocument -> userHasFileReactiveRepository.findByIdFileId(fileDocument.getFileId()).subscribe())
                .then();

    }

    @Override
    public Flux<FileDocument> getByUserLogin(String login) {
        return userHasFileReactiveRepository.findByIdLogin(login)
                .flatMap(userHasFileDocument -> fileReactiveRepository.findById(userHasFileDocument.getId().getFileId()));
    }
}
