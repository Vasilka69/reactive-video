package ru.vasili4.reactive_video.service.impl;

import lombok.extern.slf4j.Slf4j;
import org.springframework.context.annotation.Lazy;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Mono;
import reactor.core.publisher.SynchronousSink;
import ru.vasili4.reactive_video.client.vk.VkVisionClient;
import ru.vasili4.reactive_video.data.model.reactive.mongo.CachedImageRecognitionDocument;
import ru.vasili4.reactive_video.data.model.reactive.mongo.FileDocument;
import ru.vasili4.reactive_video.data.repository.reactive.CachedImageRecognitionReactiveRepository;
import ru.vasili4.reactive_video.data.repository.reactive.FileReactiveRepository;
import ru.vasili4.reactive_video.exception.ImageRecognitionException;
import ru.vasili4.reactive_video.service.FileService;
import ru.vasili4.reactive_video.service.ImageRecognitionService;
import ru.vasili4.reactive_video.utils.FileUtils;
import ru.vasili4.reactive_video.web.dto.response.ImageRecognitionResponse;

@Slf4j
@Service
public class ImageRecognitionServiceImpl implements ImageRecognitionService {

    private final FileReactiveRepository fileReactiveRepository;
    private final CachedImageRecognitionReactiveRepository cachedImageRecognitionReactiveRepository;
    private final FileService fileService;
    private final VkVisionClient vkVisionClient;

    public ImageRecognitionServiceImpl(FileReactiveRepository fileReactiveRepository, CachedImageRecognitionReactiveRepository cachedImageRecognitionReactiveRepository, FileService fileService,
                                       @Lazy VkVisionClient vkVisionClient) {
        this.fileReactiveRepository = fileReactiveRepository;
        this.cachedImageRecognitionReactiveRepository = cachedImageRecognitionReactiveRepository;
        this.fileService = fileService;
        this.vkVisionClient = vkVisionClient;
    }

    @Override
    public Mono<ImageRecognitionResponse> recognizeById(String id) {
        return fileReactiveRepository.findById(id)
                .handle((FileDocument fileDocument, SynchronousSink<FileDocument> sink) -> {
                    if (!FileUtils.isImageFile(fileDocument.getFilePath())) {
                        sink.error(new ImageRecognitionException("Файл для распознавания объектов должен быть формата изображения (%s)"
                                .formatted(FileUtils.IMAGE_EXTENSIONS)));
                    } else {
                    sink.next(fileDocument);
                }})
                .map(FileDocument::getFileId)
                .map(fileService::asyncGetFullFileContentById)
                .flatMap(vkVisionClient::detect)
                .map(detectResponse -> new CachedImageRecognitionDocument(id, detectResponse))
                .flatMap(cachedImageRecognitionReactiveRepository::save)
                .map(ImageRecognitionResponse::new);
    }

    @Override
    public Mono<ImageRecognitionResponse> cachedRecognizeById(String id) {
        return cachedImageRecognitionReactiveRepository.findById(id)
                .map(ImageRecognitionResponse::new)
                .switchIfEmpty(recognizeById(id));
    }
}
